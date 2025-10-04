package mcdodik.springai.advisors

import mcdodik.springai.advisors.config.ChatModelPrompts
import mcdodik.springai.advisors.config.VectorAdvisorProperties
import mcdodik.springai.config.Loggable
import mcdodik.springai.rag.model.Metadata
import mcdodik.springai.rag.service.api.ContextBuilder
import mcdodik.springai.rag.service.api.Reranker
import mcdodik.springai.rag.service.api.Retriever
import mcdodik.springai.rag.service.api.SummaryService
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.embedding.EmbeddingModel
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class HybridAdvisor(
    private val properties: VectorAdvisorProperties,
    private val embeddingModel: EmbeddingModel,
    private val retriever: Retriever,
    private val reranker: Reranker,
    private val contextBuilder: ContextBuilder,
    private val summaryService: SummaryService,
) : BaseAdvisor,
    Loggable {
    override fun before(
        req: ChatClientRequest,
        chain: AdvisorChain,
    ): ChatClientRequest {
        val original = req.prompt

        val userMsg = original.instructions.lastOrNull { it is UserMessage } as? UserMessage ?: return req
        val userPrompt = userMsg.text.trim()
        if (userPrompt.isBlank()) {
            return req
        }

        val t0 = System.nanoTime()
        val raw =
            try {
                retriever.retrieve(
                    query = userPrompt,
                    topK = properties.topK,
                    threshold = properties.vectorStoreSimilarityThreshold,
                )
            } catch (e: Exception) {
                logger.error("Hybrid retrieve failed", e)
                return req
            }
        if (raw.isEmpty()) {
            return req
        }

        val userEmb =
            try {
                embeddingModel.embed(userPrompt)
            } catch (e: Throwable) {
                logger.error("Embed failed", e)
                return req
            }

        val reranked =
            reranker
                .rerank(userEmb, raw)
                .asSequence()
                .filter { it.score.isFinite() && it.score >= properties.rerankSimilarityThreshold }
                .sortedByDescending { it.score }
                .toList()

        val deduped = reranker.dedup(reranked)
        val docs = deduped.take(properties.finalK).map { it.doc }
        if (docs.isEmpty()) {
            return req
        }

        val fileNames = docs.mapNotNull { Metadata.fileName(it) }.toSortedSet()
        val summaries = runCatching { summaryService.summariesByFileName(fileNames) }.getOrElse { emptyMap() }

        val docSummary =
            buildString {
                fileNames.forEach { fn ->
                    val s = summaries[fn].orEmpty()
                    if (s.isNotBlank()) {
                        appendLine(s)
                        appendLine()
                    }
                }
            }

        val ragContext = contextBuilder.build(docs, properties.maxContextChars)
        val ragSystem =
            SystemMessage(
                ChatModelPrompts.ragPrompt(
                    docSummary = docSummary,
                    context = ragContext,
                    question = userPrompt,
                ),
            )
        val alreadyAdded = original.instructions.any { it is SystemMessage && it.text == ragSystem.text }
        val mutatedPrompt =
            if (alreadyAdded) {
                original
            } else {
                Prompt
                    .builder()
                    .messages(original.instructions)
                    .messages(ragSystem)
                    .build()
            }

        val tookMs = (System.nanoTime() - t0).toDuration(DurationUnit.NANOSECONDS).inWholeMilliseconds
        logger.info(
            "RAG-Hybrid: raw={}, used={}, files={}, ctxLen={}, sumLen={}, took={} ms",
            raw.size,
            docs.size,
            fileNames.size,
            ragContext.length,
            docSummary.length,
            tookMs,
        )

        return req.mutate().prompt(mutatedPrompt).build()
    }

    override fun after(
        resp: ChatClientResponse,
        chain: AdvisorChain,
    ) = resp

    override fun getOrder(): Int = properties.order

    companion object {
        const val TO_SECOND = 1_000_000
    }
}
