package mcdodik.springai.rag.application

import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.model.Metadata
import mcdodik.springai.rag.model.ScoredDoc

class DefaultReranker(
    private val embeddingModel: org.springframework.ai.ollama.OllamaEmbeddingModel
) : Reranker {

    override fun rerank(
        userEmbedding: FloatArray,
        raw: List<org.springframework.ai.document.Document>
    ): List<ScoredDoc> =
        raw.mapNotNull { d ->
            val emb = Metadata.embedding(d) ?: return@mapNotNull null
            if (emb.size != userEmbedding.size) return@mapNotNull null
            val sim = cosineSimilarity(userEmbedding, emb)
            if (!sim.isFinite()) return@mapNotNull null
            ScoredDoc(d, sim)
        }.sortedByDescending { it.score }

    override fun dedup(scored: List<ScoredDoc>): List<ScoredDoc> =
        scored.distinctBy { Metadata.fileName(it.doc) to Metadata.chunkIndex(it.doc) }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        var dot = 0.0;
        var na = 0.0;
        var nb = 0.0
        for (i in a.indices) {
            val av = a[i].toDouble();
            val bv = b[i].toDouble()
            dot += av * bv; na += av * av; nb += bv * bv
        }
        val denom = kotlin.math.sqrt(na * nb)
        return if (denom == 0.0) Double.NEGATIVE_INFINITY else dot / denom
    }
}