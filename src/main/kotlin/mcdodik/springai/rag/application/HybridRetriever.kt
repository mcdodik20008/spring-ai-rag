package mcdodik.springai.rag.application

import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.model.FuseMode
import mcdodik.springai.rag.model.HybridConfig
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoreType

class HybridRetriever(
    private val vector: Retriever,
    private val bm25: Retriever,
    private val cfg: HybridConfig = HybridConfig()
) : Retriever {

    override fun retrieve(query: String, topK: Int, threshold: Double?): List<RetrievedDoc> {
        if (query.isBlank()) return emptyList()

        val v = vector.retrieve(query, cfg.vecTopK, threshold)
        val b = bm25.retrieve(query, cfg.bmTopK, null)

        val fused = when (cfg.mode) {
            FuseMode.RRF -> fuseRrf(v, b, cfg.rrfK)
            FuseMode.WEIGHTED -> fuseWeighted(v, b, cfg.vecWeight, cfg.bmWeight)
        }

        return fused
            .sortedByDescending { it.score }
            .take(topK.coerceAtMost(cfg.finalTopK))
    }

    /** RRF: score = Σ 1/(k + rank) */
    private fun fuseRrf(vec: List<RetrievedDoc>, bm: List<RetrievedDoc>, k: Int): List<RetrievedDoc> {
        fun ranks(list: List<RetrievedDoc>) = list.mapIndexed { i, d -> d.id to (i + 1) }.toMap()
        val vr = ranks(vec)
        val br = ranks(bm)
        val ids = (vr.keys + br.keys).toSet()

        return ids.map { id ->
            val vRank = vr[id]
            val bRank = br[id]
            val vPart = vRank?.let { 1.0 / (k + it) } ?: 0.0
            val bPart = bRank?.let { 1.0 / (k + it) } ?: 0.0
            val any = (vec.find { it.id == id } ?: bm.find { it.id == id })!!
            RetrievedDoc(
                id = id,
                content = any.content,
                metadata = any.metadata + mapOf("rrfVecRank" to vRank, "rrfBmRank" to bRank),
                score = vPart + bPart,
                type = ScoreType.HYBRID
            )
        }
    }

    /** Weighted: min–max нормализация и взвешивание */
    private fun fuseWeighted(vec: List<RetrievedDoc>, bm: List<RetrievedDoc>, vw: Double, bw: Double): List<RetrievedDoc> {
        fun normalize01(xs: List<RetrievedDoc>): List<RetrievedDoc> {
            if (xs.isEmpty()) return xs
            val minS = xs.minOf { it.score }
            val maxS = xs.maxOf { it.score }
            val denom = (maxS - minS).takeIf { it > 1e-12 } ?: 1.0
            return xs.map { it.copy(score = (it.score - minS) / denom) }
        }

        val vN = normalize01(vec)
        val bN = normalize01(bm)
        val byId = linkedMapOf<String, MutableList<RetrievedDoc>>()
        (vN + bN).forEach { d -> byId.computeIfAbsent(d.id) { mutableListOf() }.add(d) }

        return byId.map { (id, docs) ->
            val any = docs.first()
            val v = docs.firstOrNull { it.type == ScoreType.VECTOR }?.score ?: 0.0
            val m = docs.firstOrNull { it.type == ScoreType.BM25 }?.score ?: 0.0
            val s = vw * v + bw * m
            RetrievedDoc(
                id = id,
                content = any.content,
                metadata = any.metadata + mapOf("vectorNorm" to v, "bm25Norm" to m),
                score = s,
                type = ScoreType.HYBRID
            )
        }
    }
}