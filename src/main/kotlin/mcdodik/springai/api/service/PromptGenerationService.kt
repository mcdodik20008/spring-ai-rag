package mcdodik.springai.api.service

import java.time.LocalDateTime
import java.util.UUID
import mcdodik.springai.config.Loggable
import mcdodik.springai.config.chatmodel.ChatModelPrompts
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType
import mcdodik.springai.db.entity.prompt.ChunkingPromptTemplate
import mcdodik.springai.db.mybatis.mapper.ChunkingPromptTemplateMapper
import mcdodik.springai.extensions.extractBetween
import mcdodik.springai.extensions.sanitize
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.stereotype.Service

@Service
class PromptGenerationService(
    private val dynamicOpenRouterChatClient: (LLMTaskType?, String?) -> ChatClient,
    private val mapper: ChunkingPromptTemplateMapper,
    private val embeddingModel: OllamaEmbeddingModel,
) {
    fun generatePrompt(domainName: String, userDescription: String): ChunkingPromptTemplate {
        require(domainName.isNotBlank()) { "domainName is blank" }
        require(userDescription.isNotBlank()) { "userDescription is blank" }

        val sanitizedDesc = userDescription.sanitize()

        // 1. Генерация системного промпта
        val systemPrompt = ChatModelPrompts.generateChunkingPrompt(domainName, sanitizedDesc)
        logger.debug("System prompt:\n$systemPrompt")

        // 2. Формируем Prompt для LLM
        val prompt = Prompt(
            listOf(
                SystemMessage(systemPrompt),
                UserMessage(userDescription)
            )
        )

        // 3. Запрашиваем LLM
        val chatClient = dynamicOpenRouterChatClient(LLMTaskType.PROMPT_GEN, null)
        val rawResponse = chatClient.prompt(prompt).call().content().orEmpty()
        logger.debug("Raw LLM response:\n$rawResponse")

        // 4. Пытаемся вырезать из ответа по тегам
        val generated = rawResponse.extractBetween("<<<PROMPT_BEGIN", "PROMPT_END>>>")
            .ifBlank {
                logger.warn("No prompt extracted from LLM, using fallback")
                fallbackPrompt(domainName, sanitizedDesc)
            }
            .trim()
            .take(MAX_PROMPT_CHARS)

        // 5. Генерация вектора для темы (чтобы findByTopic работал)
        val topicEmbedding = try {
            embeddingModel.embed(domainName).toList()
        } catch (e: Exception) {
            logger.error("Failed to generate topic embedding")
            emptyList()
        }

        // 6. Создаём сущность
        val entity = ChunkingPromptTemplate(
            id = UUID.randomUUID(),
            domainName = domainName,
            userDescription = userDescription,
            generatedPrompt = generated,
            topicEmbedding = topicEmbedding,
            createdAt = LocalDateTime.now()
        )

        // 7. Сохраняем
        mapper.insert(entity)
        logger.info("Prompt template saved: ${entity.id} (topicEmbedding size=${topicEmbedding.size})")

        return entity
    }

    fun findByTopic(topic: String, k: Int = 5, minSim: Double = 0.35): List<Result> {
        val q = topic.trim()
        require(q.isNotEmpty()) { "topic is blank" }

        // Эмбеддинг запроса
        val qEmb = embeddingModel.embed(q).toList()

        // ANN из БД (берём с запасом)
        val ann = mapper.searchByTopicEmbedding(qEmb, k = k * 2)

        // Пересчитываем similarity локально как 1 - distance (для cosine)
        val reranked = ann.map { t ->
            val dist = cosineDistance(qEmb, t.topicEmbedding ?: emptyList())
            val sim = if (dist.isFinite()) 1.0 - dist else -1.0
            t to sim
        }
            .sortedByDescending { it.second }
            .filter { it.second >= minSim }
            .take(k)

        if (reranked.isNotEmpty()) {
            return reranked.map { Result(it.first, it.second) }
        }

        // Текстовый fallback (ILIKE + trgm)
        val like = mapper.searchByTopicLike(q, k)
        return like.map { Result(it, 0.0) }
    }

    // distance = 1 - cosineSimilarity; вернёт +∞ если что-то пошло не так
    private fun cosineDistance(a: List<Float>, b: List<Float>): Double {
        if (a.isEmpty() || b.isEmpty() || a.size != b.size) return Double.POSITIVE_INFINITY
        var dot = 0.0;
        var na = 0.0;
        var nb = 0.0
        for (i in a.indices) {
            val x = a[i].toDouble();
            val y = b[i].toDouble()
            dot += x * y; na += x * x; nb += y * y
        }
        val denom = kotlin.math.sqrt(na) * kotlin.math.sqrt(nb)
        if (denom == 0.0) return Double.POSITIVE_INFINITY
        val cos = (dot / denom).coerceIn(-1.0, 1.0)
        return 1.0 - cos
    }

    private fun fallbackPrompt(domainName: String, userDesc: String): String = """
        Ты интеллектуальный редактор, готовящий длинный текст для поиска по векторной базе (RAG) в области «$domainName».
        Используй специфику и терминологию: ${userDesc.take(MAX_TOKENS_USER_DESC)}.
        Твоя задача: выделять самодостаточные смысловые блоки (chunk’и) объёмом 300–800 токенов.
        Между блоками ставь разделитель: \n-----\n
        Удаляй служебный мусор (шапки, номера страниц, дисклеймеры), избегай риторики и повторов.
        Сохраняй факты, определения, аргументы, формулы, псевдокод и важные шаги алгоритмов/процедур.
        Списки, таблицы, код и формулы не разрывай — оформляй как единый блок.
        Пиши строго и лаконично, ориентируйся на максимальную полезность для векторного поиска.
    """.trimIndent()

    data class Result(
        val template: ChunkingPromptTemplate,
        val score: Double // 0..1 (косинусная схожесть)
    )

    companion object : Loggable {
        const val MAX_PROMPT_CHARS = 4000
        const val MAX_TOKENS_USER_DESC = 2000
    }
}
