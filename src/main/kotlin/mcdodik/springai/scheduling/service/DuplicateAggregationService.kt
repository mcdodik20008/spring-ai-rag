package mcdodik.springai.scheduling.service

import mcdodik.springai.scheduling.config.DedupProperties
import mcdodik.springai.scheduling.mapper.TfidfMapper
import mcdodik.springai.scheduling.model.ChunkForDedup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class DuplicateAggregationService(
    private val mapper: TfidfMapper,
    private val props: DedupProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun mergeDuplicates() {
        var offset = 0
        var processed = 0
        val page = props.pageSize

        while (true) {
            val batch: List<ChunkForDedup> = mapper.selectAllWithTfidf(limit = page, offset = offset)
            if (batch.isEmpty()) break

            for (ch in batch) {
                val tfidf = ch.tfidf
                val norm = ch.tfidfNorm
                if (tfidf.isEmpty() || norm == 0.0) continue

                val topTerms =
                    tfidf.entries
                        .asSequence()
                        .sortedByDescending { it.value }
                        .take(props.topTermsPerDoc)
                        .map { it.key }
                        .toList()
                if (topTerms.isEmpty()) continue

                // XML-маппер принимает List<String>, внутри строит CTE terms(t)
                val candidates = mapper.findCandidatesByAnyTerms(ch.id, topTerms, props.candidateLimit)

                var best: Pair<UUID, Double>? = null
                for (cand in candidates) {
                    val sim = cosineSparse(tfidf, norm, cand.tfidf, cand.tfidfNorm)
                    if (sim >= props.similarityThreshold) {
                        if (best == null || sim > best.second) best = cand.id to sim
                    }
                }

                if (best != null) {
                    val (dup, sim) = best
                    // Правило: всегда держим меньший UUID как "канон", это избегает циклов
                    val keepId = minOf(ch.id, dup)
                    val dupId = maxOf(ch.id, dup)
                    mapper.upsertDuplicate(dupId = dupId, keepId = keepId, simScore = sim)
                }
            }

            offset += batch.size
            processed += batch.size
            log.info("Dedup pass: processed page={}, total={}", batch.size, processed)
        }
        log.info("Dedup finished, total={}", processed)
    }

    private fun cosineSparse(
        a: Map<String, Double>,
        aNorm: Double,
        b: Map<String, Double>,
        bNorm: Double,
    ): Double {
        if (aNorm == 0.0 || bNorm == 0.0) return 0.0
        val (small, big) = if (a.size <= b.size) a to b else b to a
        var dot = 0.0
        for ((t, wa) in small) {
            val wb = big[t] ?: continue
            dot += wa * wb
        }
        return dot / (aNorm * bNorm)
    }
}
