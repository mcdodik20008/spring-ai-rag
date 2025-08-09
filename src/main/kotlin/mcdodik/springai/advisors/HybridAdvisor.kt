package mcdodik.springai.advisors

import mcdodik.springai.config.Loggable
import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.config.chatmodel.ChatModelPrompts
import mcdodik.springai.rag.api.ContextBuilder
import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.api.SummaryService
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.ollama.OllamaEmbeddingModel
import mcdodik.springai.rag.model.Metadata
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.prompt.Prompt

class HybridAdvisor(
    private val properties: VectorAdvisorProperties,
    private val embeddingModel: OllamaEmbeddingModel,
    private val retriever: Retriever,
    private val reranker: Reranker,
    private val contextBuilder: ContextBuilder,
    private val summaryService: SummaryService
) : BaseAdvisor, Loggable {

    override fun before(req: ChatClientRequest, chain: AdvisorChain): ChatClientRequest {
        val userPrompt = req.prompt.userMessage.text
        if (userPrompt.isBlank()) return req

        val t0 = System.nanoTime()
        val raw = try {
            retriever.retrieve(
                query = userPrompt,
                topK = properties.topK,
                threshold = properties.vectorStoreSimilarityThreshold
            )
        } catch (e: Exception) {
            logger.error("Hybrid retrieve failed", e)
            return req
        }
        if (raw.isEmpty()) return req

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
        val summaries = runCatching { summaryService.summariesByFileName(fileNames) }.getOrElse { emptyMap() }

        val docSummary = buildString {
            fileNames.forEach { fn ->
                val s = summaries[fn].orEmpty()
                if (s.isNotBlank()) appendLine(s).also { appendLine() }
            }
        }
        val ragContext = contextBuilder.build(docs, properties.maxContextChars)
        val content = ChatModelPrompts.ragPrompt(docSummary, ragContext, userPrompt)

        val mutated = req.mutate().prompt(Prompt.builder().content(content).build()).build()

        logger.info(
            "RAG-Hybrid: raw={}, used={}, files={}, ctxLen={}, sumLen={}, took={} ms",
            raw.size, docs.size, fileNames.size, ragContext.length, docSummary.length,
            (System.nanoTime() - t0) / 1_000_000
        )
        return mutated
    }

    override fun after(resp: ChatClientResponse, chain: AdvisorChain) = resp
    override fun getOrder(): Int = properties.order
}