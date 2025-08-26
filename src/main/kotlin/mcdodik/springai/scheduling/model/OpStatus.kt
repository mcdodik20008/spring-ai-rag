package mcdodik.springai.scheduling.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Статус выполненной операции")
data class OpStatus(
    @field:Schema(description = "Операция", example = "tfidf-recompute")
    val op: String,
    @field:Schema(description = "Время старта", format = "date-time", example = "2025-08-26T07:41:00Z")
    val startedAt: Instant,
    @field:Schema(description = "Время окончания", format = "date-time", example = "2025-08-26T07:41:42Z")
    val finishedAt: Instant,
    @field:Schema(description = "Длительность, мс", example = "42011", minimum = "0")
    val durationMs: Long,
)
