package mcdodik.springai.rag.service.impl

import mcdodik.springai.rag.model.Metadata
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoredDoc
import mcdodik.springai.rag.service.api.Reranker

class DefaultReranker : Reranker {
    override fun rerank(
        userEmbedding: FloatArray,
        raw: List<RetrievedDoc>,
    ): List<ScoredDoc> =
        raw
            .mapNotNull { doc ->
                val emb = Metadata.embedding(doc) ?: return@mapNotNull null
                if (emb.size != userEmbedding.size) return@mapNotNull null

                val sim = cosineSimilarity(userEmbedding, emb)
                if (!sim.isFinite()) return@mapNotNull null

                ScoredDoc(doc, sim)
            }.sortedByDescending { it.score }

    override fun dedup(scored: List<ScoredDoc>): List<ScoredDoc> = scored.distinctBy { Metadata.fileName(it.doc) to Metadata.chunkIndex(it.doc) }

    private fun cosineSimilarity(
        a: FloatArray,
        b: FloatArray,
    ): Double {
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
}
