package mcdodik.springai.advisors

import mcdodik.springai.config.Loggable
import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.config.chatmodel.ChatModelPrompts
import mcdodik.springai.rag.api.ContextBuilder
import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.api.SummaryService
import mcdodik.springai.rag.model.Metadata
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaEmbeddingModel

/**
 * Advisor для обогащения пользовательского запроса (prompt) через RAG-пайплайн.
 *
 * Логика работы:
 * 1. Проверяем, что в userPrompt есть текст. Если пусто — пропускаем обработку.
 * 2. Вызываем Retriever для поиска topK релевантных документов в векторном хранилище
 *    по порогу схожести (`vectorStoreSimilarityThreshold`).
 * 3. Строим эмбеддинг запроса через OllamaEmbeddingModel.
 * 4. Прогоняем найденные документы через Reranker для уточнения порядка по сходству с запросом.
 * 5. Отфильтровываем документы по порогу rerank-похожести (`rerankSimilarityThreshold`) и удаляем дубликаты.
 * 6. Оставляем не более `finalK` лучших документов.
 * 7. Извлекаем уникальные fileName этих документов, получаем их краткие summary через SummaryService.
 * 8. Формируем:
 *    - docSummary — объединённые summary по всем файлам,
 *    - ragContext — выжимку фактического контента документов (через ContextBuilder).
 * 9. Создаём RAG-промпт (docSummary + ragContext + исходный userPrompt) через ChatModelPrompts.ragPrompt().
 * 10. В существующий Prompt запроса добавляем системное сообщение с RAG-контекстом
 * 11. Возвращаем изменённый ChatClientRequest, который пойдёт дальше по цепочке Advisors.
 */
class VectorAdvisor(
    private val properties: VectorAdvisorProperties,
    private val embeddingModel: OllamaEmbeddingModel,
    private val retriever: Retriever,
    private val reranker: Reranker,
    private val contextBuilder: ContextBuilder,
    private val summaryService: SummaryService
) : BaseAdvisor, Loggable {

    override fun before(req: ChatClientRequest, chain: AdvisorChain): ChatClientRequest {
        val userPrompt = req.prompt.userMessage.text
        if (userPrompt.isBlank()) {
            logger.warn("Empty user prompt, skipping RAG enrichment")
            return req
        }
        val t0 = System.nanoTime()

        val raw = try {
            retriever.retrieve(
                query = userPrompt,
                topK = properties.topK,
                threshold = properties.vectorStoreSimilarityThreshold
            )
        } catch (e: Exception) {
            logger.error("Retrieve failed", e)
            return req // graceful degradation
        }
        if (raw.isEmpty()) {
            if (logger.isDebugEnabled) logger.debug("RAG: no hits")
            return req
        }
        logger.debug("RAG: raw hits {}", raw.size)

        val userEmb = embeddingModel.embed(userPrompt)
        val reranked = reranker.rerank(userEmb, raw)
            .asSequence()
            .filter { it.score.isFinite() && it.score >= properties.rerankSimilarityThreshold }
            .sortedByDescending { it.score }
            .toList()

        val deduped = reranker.dedup(reranked)
        val docs = deduped.take(properties.finalK).map { it.doc }
        if (docs.isEmpty()) return req

        val fileNames = docs.mapNotNull { Metadata.fileName(it) }.toSet()
        logger.debug("Selected document files: {}", fileNames.joinToString())

        val summaries = try {
            summaryService.summariesByFileName(fileNames)
        } catch (e: Exception) {
            logger.warn("Summaries failed, continue without", e)
            emptyMap<String, String>()
        }

        val docSummary = buildString {
            fileNames.forEach { fn ->
                val s = summaries[fn].orEmpty()
                if (s.isNotBlank()) {
                    append(s)
                    appendLine()
                    appendLine()
                }
            }
        }
        val ragContext = contextBuilder.build(docs, properties.maxContextChars)

        val content = ChatModelPrompts.ragPrompt(docSummary, ragContext, userPrompt)
        val mutated = req.mutate().prompt(Prompt.builder().content(content).build()).build()

        logger.info(
            "RAG: raw={}, used={}, files={}, ctxLen={}, sumLen={}, took={} ms",
            raw.size, docs.size, fileNames.size, ragContext.length, docSummary.length,
            (System.nanoTime() - t0) / 1_000_000
        )
        return mutated
    }

    override fun after(resp: ChatClientResponse, chain: AdvisorChain) = resp

    override fun getOrder(): Int = properties.order
}