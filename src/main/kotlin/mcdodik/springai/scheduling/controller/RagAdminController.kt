package mcdodik.springai.scheduling.controller

import mcdodik.springai.scheduling.model.CombinedStatus
import mcdodik.springai.scheduling.model.OpStatus
import mcdodik.springai.scheduling.service.DuplicateAggregationService
import mcdodik.springai.scheduling.service.TfidfRecomputeService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock

@RestController
@RequestMapping("/rag")
class RagAdminController(
    private val tfidf: TfidfRecomputeService,
    private val dedup: DuplicateAggregationService,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val tfidfLock = ReentrantLock(true)
    private val dedupLock = ReentrantLock(true)

    @PostMapping("/tfidf/recompute")
    fun recomputeTfidf(): Mono<OpStatus> =
        Mono
            .fromCallable { runExclusive("tfidf-recompute", tfidfLock) { tfidf.recomputeAll() } }
            .subscribeOn(Schedulers.boundedElastic())

    @PostMapping("/dedup/merge")
    fun mergeDedup(): Mono<OpStatus> =
        Mono
            .fromCallable { runExclusive("dedup-merge", dedupLock) { dedup.mergeDuplicates() } }
            .subscribeOn(Schedulers.boundedElastic())

    @PostMapping("/reindex-all")
    fun reindexAll(): Mono<CombinedStatus> =
        Mono
            .fromCallable {
                val startAll = Instant.now()
                val r1 = runExclusive("tfidf-recompute", tfidfLock) { tfidf.recomputeAll() }
                val r2 = runExclusive("dedup-merge", dedupLock) { dedup.mergeDuplicates() }
                val total = Duration.between(startAll, Instant.now()).toMillis()
                CombinedStatus(results = listOf(r1, r2), totalDurationMs = total)
            }.subscribeOn(Schedulers.boundedElastic())

    private inline fun runExclusive(
        opName: String,
        lock: ReentrantLock,
        block: () -> Unit,
    ): OpStatus {
        if (!lock.tryLock()) {
            log.warn("Operation {} is already running", opName)
            throw ResponseStatusException(HttpStatus.CONFLICT, "Operation $opName is already running")
        }
        val started = Instant.now()
        return try {
            block()
            OpStatus(
                op = opName,
                startedAt = started,
                finishedAt = Instant.now(),
                durationMs = Duration.between(started, Instant.now()).toMillis(),
            )
        } catch (e: Exception) {
            log.error("Operation {} failed", opName, e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Operation $opName failed: ${e.message}")
        } finally {
            lock.unlock()
        }
    }
}
