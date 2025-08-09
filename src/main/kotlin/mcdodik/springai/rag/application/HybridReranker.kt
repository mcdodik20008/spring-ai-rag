package mcdodik.springai.rag.application

import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.model.Metadata
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoreType
import mcdodik.springai.rag.model.ScoredDoc
import kotlin.math.sqrt

class HybridReranker(
    private val wVec: Double = 0.6,
    private val wBm25: Double = 0.4,
    private val wBase: Double = 0.0,
    private val minDimMatch: Boolean = true,
) : Reranker {
    companion object {
        private const val EPS = 1e-12
        private const val MIN_NORM = 0.0
        private const val MAX_NORM = 1.0
        private const val DEFAULT_MIN = 0.0
        private const val DEFAULT_MAX = 1.0
    }

    override fun rerank(
        userEmbedding: FloatArray,
        raw: List<RetrievedDoc>,
    ): List<ScoredDoc> {
        if (raw.isEmpty()) return emptyList()

        val bmList = raw.filter { it.type == ScoreType.BM25 }
        val baseScores = raw.map { it.score }
        val baseMin = baseScores.minOrNull() ?: DEFAULT_MIN
        val baseMax = baseScores.maxOrNull() ?: DEFAULT_MAX
        val baseDen = (baseMax - baseMin).takeIf { it > EPS } ?: DEFAULT_MAX

        val bmScores = bmList.map { it.score }
        val bmMin = bmScores.minOrNull() ?: DEFAULT_MIN
        val bmMax = bmScores.maxOrNull() ?: DEFAULT_MAX
        val bmDen = (bmMax - bmMin).takeIf { it > EPS } ?: DEFAULT_MAX

        fun normBase(s: Double) = ((s - baseMin) / baseDen).coerceIn(MIN_NORM, MAX_NORM)

        fun normBm(s: Double) = ((s - bmMin) / bmDen).coerceIn(MIN_NORM, MAX_NORM)

        val scored =
            raw.map { rd ->
                // Вытаскиваем эмбеддинг документа из метаданных, если он там есть
                toScoredDoc(rd, userEmbedding, ::normBase, ::normBm)
            }

        return scored
            .asSequence()
            .filter { it.score.isFinite() }
            .sortedByDescending { it.score }
            .toList()
    }

    private fun toScoredDoc(
        rd: RetrievedDoc,
        userEmbedding: FloatArray,
        normBase: (Double) -> Double,
        normBm: (Double) -> Double,
    ): ScoredDoc {
        val docEmb =
            (rd.metadata["embedding"] as? FloatArray)
                ?: (rd.metadata["embedding"] as? List<Float>)?.toFloatArray()

        val vecSimNorm =
            if (docEmb != null && (!minDimMatch || docEmb.size == userEmbedding.size)) {
                val cos = cosineSimilarity(userEmbedding, docEmb)
                if (cos.isFinite()) {
                    ((cos + 1.0) / 2.0).coerceIn(0.0, 1.0)
                } else {
                    0.0
                }
            } else {
                0.0
            }

        val bmNorm = if (rd.type == ScoreType.BM25) normBm(rd.score) else 0.0
        val baseNorm = normBase(rd.score)

        val final = (wVec * vecSimNorm) + (wBm25 * bmNorm) + (wBase * baseNorm)

        return ScoredDoc(rd, final)
    }

    override fun dedup(scored: List<ScoredDoc>): List<ScoredDoc> =
        scored.distinctBy { Metadata.fileName(it.doc) to Metadata.chunkIndex(it.doc) }

    private fun cosineSimilarity(
        a: FloatArray,
        b: FloatArray,
    ): Double {
        var dot = 0.0
        var na = 0.0
        var nb = 0.0
        val n = minOf(a.size, b.size)
        for (i in 0 until n) {
            val x = a[i].toDouble()
            val y = b[i].toDouble()
            dot += x * y
            na += x * x
            nb += y * y
        }
        val denom = sqrt(na) * sqrt(nb)
        return if (denom > EPS) dot / denom else Double.NaN
    }
}
