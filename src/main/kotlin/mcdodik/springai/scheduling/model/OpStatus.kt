package mcdodik.springai.scheduling.model

import java.time.Instant

data class OpStatus(
    val op: String,
    val startedAt: Instant,
    val finishedAt: Instant,
    val durationMs: Long,
    val message: String = "OK",
)
