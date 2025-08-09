package mcdodik.springai.rag.application

import kotlin.math.sqrt
import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.model.Metadata
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoreType
import mcdodik.springai.rag.model.ScoredDoc

class HybridReranker(
    private val wVec: Double = 0.6,
    private val wBm25: Double = 0.4,
    private val wBase: Double = 0.0,      // можно включить, если хочешь учитывать fused/RRF score
    private val minDimMatch: Boolean = true
) : Reranker {

    override fun rerank(
        userEmbedding: FloatArray,
        raw: List<RetrievedDoc>
    ): List<ScoredDoc> {
        if (raw.isEmpty()) return emptyList()

        // Подготовим нормализации по батчу
        val bmList = raw.filter { it.type == ScoreType.BM25 }
        val baseScores = raw.map { it.score }
        val baseMin = baseScores.minOrNull() ?: 0.0
        val baseMax = baseScores.maxOrNull() ?: 1.0
        val baseDen = (baseMax - baseMin).takeIf { it > 1e-12 } ?: 1.0

        val bmScores = bmList.map { it.score }
        val bmMin = bmScores.minOrNull() ?: 0.0
        val bmMax = bmScores.maxOrNull() ?: 1.0
        val bmDen = (bmMax - bmMin).takeIf { it > 1e-12 } ?: 1.0

        fun normBase(s: Double) = ((s - baseMin) / baseDen).coerceIn(0.0, 1.0)
        fun normBm(s: Double) = ((s - bmMin) / bmDen).coerceIn(0.0, 1.0)

        val scored = raw.map { rd ->
            // Вытаскиваем эмбеддинг документа из метаданных, если он там есть
            val docEmb = (rd.metadata["embedding"] as? FloatArray)
                ?: (rd.metadata["embedding"] as? List<Float>)?.toFloatArray()

            val vecSimNorm = if (docEmb != null && (!minDimMatch || docEmb.size == userEmbedding.size)) {
                val cos = cosineSimilarity(userEmbedding, docEmb)
                if (cos.isFinite()) ((cos + 1.0) / 2.0).coerceIn(0.0, 1.0) else 0.0
            } else 0.0

            val bmNorm = if (rd.type == ScoreType.BM25) normBm(rd.score) else 0.0
            val baseNorm = normBase(rd.score)

            val final = (wVec * vecSimNorm) + (wBm25 * bmNorm) + (wBase * baseNorm)

            ScoredDoc(rd, final)
        }

        return scored
            .asSequence()
            .filter { it.score.isFinite() }
            .sortedByDescending { it.score }
            .toList()
    }

    override fun dedup(scored: List<ScoredDoc>): List<ScoredDoc> =
        scored.distinctBy { Metadata.fileName(it.doc) to Metadata.chunkIndex(it.doc) }


    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
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
        return if (denom > 1e-12) dot / denom else Double.NaN
    }
}