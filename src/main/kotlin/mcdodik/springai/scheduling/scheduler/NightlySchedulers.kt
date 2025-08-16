package mcdodik.springai.scheduling.scheduler

import mcdodik.springai.config.Loggable
import mcdodik.springai.scheduling.service.DuplicateAggregationService
import mcdodik.springai.scheduling.service.TfidfRecomputeService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NightlySchedulers(
    private val tfidf: TfidfRecomputeService,
    private val dedup: DuplicateAggregationService,
) : Loggable {
    @Scheduled(cron = "0 0 0 * * *") // 00:00
    fun recomputeTfidfNightly() {
        logger.info("TF-IDF recompute started")
        tfidf.recomputeAll()
        logger.info("TF-IDF recompute finished")
    }

    @Scheduled(cron = "0 0 3 * * *") // 03:00
    fun mergeDuplicatesNightly() {
        logger.info("Dedup started")
        dedup.mergeDuplicates()
        logger.info("Dedup finished")
    }
}
