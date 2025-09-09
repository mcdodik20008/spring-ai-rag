package mcdodik.springai.scheduling.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import mcdodik.springai.scheduling.model.CombinedStatus
import mcdodik.springai.scheduling.model.OpStatus
import mcdodik.springai.scheduling.service.DuplicateAggregationService
import mcdodik.springai.scheduling.service.TfidfRecomputeService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.time.Instant
import java.util.concurrent.locks.ReentrantLock

@Tag(name = "RAG Admin", description = "Административные операции индекса: TF-IDF, дедупликация, массовое переиндексирование (Не нужно тестить)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/rag", produces = [MediaType.APPLICATION_JSON_VALUE])
class RagAdminController(
    private val tfidf: TfidfRecomputeService,
    private val dedup: DuplicateAggregationService,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val tfidfLock = ReentrantLock(true)
    private val dedupLock = ReentrantLock(true)

    @Operation(
        summary = "Пересчитать TF-IDF",
        description = "Запускает пересчёт TF-IDF по всей базе. Взаимоисключающая операция; при повторном запуске вернёт 409.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ок",
                content = [
                    Content(
                        schema = Schema(implementation = OpStatus::class),
                        examples = [
                            ExampleObject(
                                name = "tfidfOk",
                                value = """
                        {
                          "op": "tfidf-recompute",
                          "startedAt": "2025-08-26T07:41:00Z",
                          "finishedAt": "2025-08-26T07:41:42Z",
                          "durationMs": 42011
                        }
                        """,
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "409", description = "Операция уже выполняется"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка"),
        ],
    )
    @PostMapping("/tfidf/recompute")
    fun recomputeTfidf(): Mono<OpStatus> =
        Mono
            .fromCallable { runExclusive("tfidf-recompute", tfidfLock) { tfidf.recomputeAll() } }
            .subscribeOn(Schedulers.boundedElastic())

    @Operation(
        summary = "Объединить дубликаты",
        description = "Агрегирует и объединяет дубликаты чанков/документов. Взаимоисключающая операция; при повторном запуске вернёт 409.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ок",
                content = [
                    Content(
                        schema = Schema(implementation = OpStatus::class),
                        examples = [
                            ExampleObject(
                                name = "dedupOk",
                                value = """
                        {
                          "op": "dedup-merge",
                          "startedAt": "2025-08-26T07:45:00Z",
                          "finishedAt": "2025-08-26T07:45:18Z",
                          "durationMs": 18234
                        }
                        """,
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "409", description = "Операция уже выполняется"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка"),
        ],
    )
    @PostMapping("/dedup/merge")
    fun mergeDedup(): Mono<OpStatus> =
        Mono
            .fromCallable { runExclusive("dedup-merge", dedupLock) { dedup.mergeDuplicates() } }
            .subscribeOn(Schedulers.boundedElastic())

    @Operation(
        summary = "Полная переиндексация (TF-IDF + дедуп)",
        description = "Последовательно запускает пересчёт TF-IDF и дедупликацию. Результат — сводка по обеим операциям.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ок",
                content = [
                    Content(
                        schema = Schema(implementation = CombinedStatus::class),
                        examples = [
                            ExampleObject(
                                name = "reindexAllOk",
                                value = """
                        {
                          "results": [
                            { "op": "tfidf-recompute", "startedAt": "2025-08-26T07:50:00Z", "finishedAt": "2025-08-26T07:50:39Z", "durationMs": 39321 },
                            { "op": "dedup-merge",     "startedAt": "2025-08-26T07:50:39Z", "finishedAt": "2025-08-26T07:50:55Z", "durationMs": 16540 }
                          ],
                          "totalDurationMs": 55861
                        }
                        """,
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "409", description = "Какая-то из операций уже выполняется"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка"),
        ],
    )
    @PostMapping("/reindex-all")
    fun reindexAll(): Mono<CombinedStatus> =
        Mono
            .fromCallable {
                val startAll = Instant.now()
                val r1 = runExclusive("tfidf-recompute", tfidfLock) { tfidf.recomputeAll() }
                val r2 = runExclusive("dedup-merge", dedupLock) { dedup.mergeDuplicates() }
                val total = Duration.between(startAll, Instant.now()).toMillis()
                CombinedStatus(results = listOf(r1, r2), totalDurationMs = total)
            }
            .subscribeOn(Schedulers.boundedElastic())

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
