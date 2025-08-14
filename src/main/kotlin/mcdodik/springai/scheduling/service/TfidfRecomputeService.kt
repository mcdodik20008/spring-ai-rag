package mcdodik.springai.scheduling.service

import mcdodik.springai.scheduling.config.DedupProperties
import mcdodik.springai.scheduling.mapper.TfidfMapper
import mcdodik.springai.scheduling.model.ChunkForTfidf
import mcdodik.springai.scheduling.model.ChunkTfidfUpdate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ln
import kotlin.math.sqrt

@Service
class TfidfRecomputeService(
    private val mapper: TfidfMapper,
    private val tokenizer: Tokenizer,
    private val props: DedupProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Постранично: вытаскиваем id+text, считаем локальный DF и затем TF-IDF для батча.
     * Пишем JSONB (Map<String, Double>) и норму в БД.
     */
    @Transactional
    fun recomputeAll() {
        var offset = 0
        val batchSize = props.recomputeBatchSize
        var processed = 0

        while (true) {
            val batch: List<ChunkForTfidf> = mapper.selectChunksNeedingTfidf(limit = batchSize, offset = offset)
            if (batch.isEmpty()) break

            // Локальный DF по батчу
            val df = mutableMapOf<String, Int>()
            val tokenized = HashMap<java.util.UUID, List<String>>(batch.size)

            for (ch in batch) {
                val toks = tokenizer.tokens(ch.text)
                tokenized[ch.id] = toks
                toks.distinct().forEach { t -> df[t] = (df[t] ?: 0) + 1 }
            }

            val n = batch.size.toDouble()
            val idf = df.mapValues { (_, dfi) -> ln((n + 1.0) / (dfi + 1.0)) + 1.0 }

            batch.forEach { ch ->
                val tokens = tokenized[ch.id].orEmpty()
                if (tokens.isEmpty()) {
                    mapper.updateTfidf(ChunkTfidfUpdate(ch.id, emptyMap(), 0.0))
                    return@forEach
                }
                val tf = tokens.groupingBy { it }.eachCount()
                val maxTf = tf.values.maxOrNull()!!.toDouble()

                val weights =
                    buildMap {
                        tf.forEach { (term, cnt) ->
                            val w = (0.5 + 0.5 * (cnt / maxTf)) * (idf[term] ?: 0.0)
                            if (w > 0.0) put(term, w)
                        }
                    }
                val norm = sqrt(weights.values.sumOf { it * it })
                mapper.updateTfidf(ChunkTfidfUpdate(ch.id, weights, norm))
            }

            offset += batch.size
            processed += batch.size
            log.info("TF-IDF recompute: processed batch={}, total={}", batch.size, processed)
        }
        log.info("TF-IDF recompute finished, total={}", processed)
    }
}
