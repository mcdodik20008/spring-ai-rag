package mcdodik.springai.advisors

import mcdodik.springai.config.Loggable
import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.config.chatmodel.ChatModelPrompts
import mcdodik.springai.db.model.rag.DocumentMetadataKey
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore

class VectorAdvisor(
    private val properties: VectorAdvisorProperties,
    private val embeddingModel: OllamaEmbeddingModel,
    private val vectorStore: VectorStore,
    private val documentMapper: DocumentInfoMapper
) : BaseAdvisor {

    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain
    ): ChatClientRequest {
        val t0 = System.nanoTime()
        val userPrompt = chatClientRequest.prompt.userMessage.text
        logger.debug("User prompt: {} and minSimilarityThreshold: {}", userPrompt, properties.rerankSimilarityThreshold)

        if (userPrompt.isBlank()) {
            logger.warn("Empty user prompt, skipping RAG enrichment")
            return chatClientRequest
        }


        // ToDo искать по самари и по расширенному промпту -
        // по самари найдем файлы, а по расширенному докинем контекста из этих файлов
        val rawDocuments = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(userPrompt)
                .topK(properties.topK)
                .similarityThreshold(properties.vectorStoreSimilarityThreshold)
                .build()
        )
        logger.debug("RAG: raw hits {}", rawDocuments.size)

        val userEmb = embeddingModel.embed(userPrompt)
        val candidates = rawDocuments.mapNotNull { doc ->
            val embAny = doc.metadata[DocumentMetadataKey.EMBEDDING.key]
            val emb = (embAny as? List<*>)?.mapNotNull { (it as? Number)?.toFloat() }?.toFloatArray()
            if (emb == null) {
                logger.debug("Skip doc without embedding: file={}", doc.metadata[DocumentMetadataKey.FILE_NAME.key])
                return@mapNotNull null
            }
            val sim = cosineSimilarity(userEmb, emb)
            if (sim.isInfinite() || sim.isNaN()) {
                logger.debug("Skip doc with invalid similarity: file={}", doc.metadata[DocumentMetadataKey.FILE_NAME.key])
                return@mapNotNull null
            }
            Pair(doc, sim)
        }


        // сортируем по убыванию similarity и фильтруем
        val reranked = candidates
            .sortedByDescending { it.second }
            .filter { it.second >= properties.rerankSimilarityThreshold }
            .map { it.first }
        logger.info("Filtered {} documents at threshold {}", reranked.size, properties.rerankSimilarityThreshold)

        val ragContext = reranked.joinToString("\n\n") { it.text ?: "" }

        val docFileNames = reranked
            .mapNotNull { it.metadata[DocumentMetadataKey.FILE_NAME.key] as? String }
            .distinct()

        logger.debug("Selected document files: {}", docFileNames.joinToString())
        val docSummary = docFileNames
            .joinToString("\n\n") { file ->
                val summary = documentMapper.searchByFilenameLike(file).summary
                logger.trace("Summary for $file: ${summary?.take(100)}")
                summary ?: ""
            }

        logger.debug("Total summary length: ${docSummary.length}, context length: ${ragContext.length}")

        val content = ChatModelPrompts.ragPrompt(docSummary, ragContext, userPrompt)

        val prompt = Prompt.builder().content(content).build()

        val mutated = chatClientRequest.mutate()
            .prompt(prompt)
            .build()

        logger.info(
            "RAG: hits={}, files={}, ctxLen={}, sumLen={}, took={} ms",
            reranked.size, docFileNames.size, ragContext.length, docSummary.length,
            (System.nanoTime() - t0) / 1_000_000
        )

        return mutated
    }


    override fun after(
        chatClientResponse: ChatClientResponse,
        advisorChain: AdvisorChain
    ): ChatClientResponse {
        return chatClientResponse
    }

    override fun getOrder(): Int {
        return properties.order ?: 0
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size) return Double.NEGATIVE_INFINITY
        var dot = 0.0
        var na = 0.0
        var nb = 0.0
        for (i in a.indices) {
            val av = a[i].toDouble()
            val bv = b[i].toDouble()
            dot += av * bv
            na += av * av
            nb += bv * bv
        }
        val denom = kotlin.math.sqrt(na * nb)
        return if (denom == 0.0) Double.NEGATIVE_INFINITY else dot / denom
    }

    companion object : Loggable
}