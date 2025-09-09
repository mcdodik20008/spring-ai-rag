package mcdodik.springai.rag.application

import mcdodik.springai.config.Loggable
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.config.HybridConfig
import mcdodik.springai.rag.model.FuseMode
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoreType
import kotlin.system.measureNanoTime

class HybridRetriever(
    private val vector: Retriever,
    private val bm25: Retriever,
    private val cfg: HybridConfig,
) : Retriever,
    Loggable {
    override fun retrieve(
        query: String,
        topK: Int,
        threshold: Double?,
    ): List<RetrievedDoc> {
        if (query.isBlank()) {
            logger.debug("HybridRetriever.retrieve: blank query → empty result")
            return emptyList()
        }

        logger.debug(
            "HybridRetriever.retrieve: start | mode={}, topK={}, finalTopK={}, " +
                "vecTopK={}, bmTopK={}, vecWeight={}, bmWeight={}, rrfK={}, threshold={}",
            cfg.mode,
            topK,
            cfg.finalTopK,
            cfg.vecTopK,
            cfg.bmTopK,
            cfg.vecWeight,
            cfg.bmWeight,
            cfg.rrfK,
            threshold,
        )

        var vTimeNs = 0L
        var bTimeNs = 0L

        val v: List<RetrievedDoc> =
            try {
                lateinit var tmp: List<RetrievedDoc>
                vTimeNs =
                    measureNanoTime {
                        tmp = vector.retrieve(query, cfg.vecTopK, threshold)
                    }
                tmp
            } catch (e: Exception) {
                logger.error("HybridRetriever.retrieve: vector.retrieve failed", e)
                emptyList()
            }

        val b: List<RetrievedDoc> =
            try {
                lateinit var tmp: List<RetrievedDoc>
                bTimeNs =
                    measureNanoTime {
                        tmp = bm25.retrieve(query, cfg.bmTopK, null)
                    }
                tmp
            } catch (e: Exception) {
                logger.error("HybridRetriever.retrieve: bm25.retrieve failed", e)
                emptyList()
            }

        logger.debug(
            "HybridRetriever.retrieve: got results | vector={} ({} ms), bm25={} ({} ms)",
            v.size,
            vTimeNs.ms(),
            b.size,
            bTimeNs.ms(),
        )

        if (v.isEmpty() && b.isEmpty()) {
            logger.warn("HybridRetriever.retrieve: both retrievers returned empty lists")
            return emptyList()
        }

        val fused: List<RetrievedDoc> =
            when (cfg.mode) {
                FuseMode.RRF -> {
                    logger.debug("HybridRetriever.retrieve: fuse mode = RRF (k={})", cfg.rrfK)
                    fuseRrf(v, b, cfg.rrfK)
                }

                FuseMode.WEIGHTED -> {
                    logger.debug(
                        "HybridRetriever.retrieve: fuse mode = WEIGHTED (vecWeight={}, bmWeight={})",
                        cfg.vecWeight,
                        cfg.bmWeight,
                    )
                    fuseWeighted(v, b, cfg.vecWeight, cfg.bmWeight)
                }
            }

        if (fused.isEmpty()) {
            logger.warn("HybridRetriever.retrieve: fused is empty (after {} mode)", cfg.mode)
            return emptyList()
        }

        val limited =
            fused
                .sortedByDescending { it.score }
                .take(topK.coerceAtMost(cfg.finalTopK))

        // Логируем топ документов (id, score, типы найденных сигналов).
        logTop("HybridRetriever.retrieve: TOP", limited, top = 10)

        logger.debug(
            "HybridRetriever.retrieve: done | fused={}, returned={}, requestedTopK={}, finalTopK={}",
            fused.size,
            limited.size,
            topK,
            cfg.finalTopK,
        )

        return limited
    }

    /** RRF: score = Σ 1/(k + rank) */
    private fun fuseRrf(
        vec: List<RetrievedDoc>,
        bm: List<RetrievedDoc>,
        k: Int,
    ): List<RetrievedDoc> {
        fun ranks(list: List<RetrievedDoc>) = list.mapIndexed { i, d -> d.id to (i + 1) }.toMap()
        val vr = ranks(vec)
        val br = ranks(bm)
        val ids = (vr.keys + br.keys).toSet()

        logger.debug(
            "HybridRetriever.fuseRrf: vec={}, bm={}, union={}",
            vec.size,
            bm.size,
            ids.size,
        )

        val fused =
            ids.map { id ->
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
                    type = ScoreType.HYBRID,
                )
            }

        if (logger.isTraceEnabled) {
            fused
                .sortedByDescending { it.score }
                .take(10)
                .forEachIndexed { i, d ->
                    val vv = d.metadata["rrfVecRank"]
                    val bb = d.metadata["rrfBmRank"]
                    logger.trace(
                        "HybridRetriever.fuseRrf: #{} id={} score={}. rrfVecRank={}, rrfBmRank={}",
                        i + 1,
                        d.id,
                        "%.5f".format(d.score),
                        vv,
                        bb,
                    )
                }
        }

        return fused
    }

    /** Weighted: min–max нормализация и взвешивание */
    private fun fuseWeighted(
        vec: List<RetrievedDoc>,
        bm: List<RetrievedDoc>,
        vw: Double,
        bw: Double,
    ): List<RetrievedDoc> {
        fun normalize01(xs: List<RetrievedDoc>): List<RetrievedDoc> {
            if (xs.isEmpty()) return xs
            val minS = xs.minOf { it.score }
            val maxS = xs.maxOf { it.score }
            val denom = (maxS - minS).takeIf { it > EPS } ?: 1.0
            logger.debug(
                "HybridRetriever.fuseWeighted.normalize01: n={}, min={}, max={}, denom={}",
                xs.size,
                "%.5f".format(minS),
                "%.5f".format(maxS),
                "%.5f".format(denom),
            )
            return xs.map { it.copy(score = (it.score - minS) / denom) }
        }

        val vN = normalize01(vec)
        val bN = normalize01(bm)

        val byId = linkedMapOf<String, MutableList<RetrievedDoc>>()
        (vN + bN).forEach { d -> byId.computeIfAbsent(d.id) { mutableListOf() }.add(d) }

        logger.debug(
            "HybridRetriever.fuseWeighted: vecN={}, bmN={}, union={}",
            vN.size,
            bN.size,
            byId.size,
        )

        val fused =
            byId.map { (id, docs) ->
                val any = docs.first()
                val v = docs.firstOrNull { it.type == ScoreType.VECTOR }?.score ?: 0.0
                val m = docs.firstOrNull { it.type == ScoreType.BM25 }?.score ?: 0.0
                val s = vw * v + bw * m
                RetrievedDoc(
                    id = id,
                    content = any.content,
                    metadata = any.metadata + mapOf("vectorNorm" to v, "bm25Norm" to m),
                    score = s,
                    type = ScoreType.HYBRID,
                )
            }

        if (logger.isTraceEnabled) {
            fused
                .sortedByDescending { it.score }
                .take(10)
                .forEachIndexed { i, d ->
                    val v = d.metadata["vectorNorm"]
                    val m = d.metadata["bm25Norm"]
                    logger.trace(
                        "HybridRetriever.fuseWeighted: #{} id={} score={}. vectorNorm={}, bm25Norm={}, vw={}, bw={}",
                        i + 1,
                        d.id,
                        "%.5f".format(d.score),
                        v,
                        m,
                        "%.2f".format(vw),
                        "%.2f".format(bw),
                    )
                }
        }

        return fused
    }

    private fun logTop(
        prefix: String,
        docs: List<RetrievedDoc>,
        top: Int,
    ) {
        docs.take(top).forEachIndexed { i, d ->
            logger.debug(
                "{} #{} id={} score={} type={}",
                prefix,
                i + 1,
                d.id,
                "%.5f".format(d.score),
                d.type,
            )
        }
        if (docs.size > top) {
            logger.debug("{} ({} more…)", prefix, docs.size - top)
        }
    }

    private fun Long.ms(): String = "%.2f".format(this / 1_000_000.0)

    companion object {
        private const val EPS = 1e-12
    }
}
