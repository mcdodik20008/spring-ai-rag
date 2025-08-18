package mcdodik.springai.config.plananalyzer

import java.time.Instant

data class CapturedPlan(
    val sql: String,
    val params: Map<Int, Any?>,
    val planJson: String,
    val capturedAt: Instant = Instant.now(),
    val caller: String? = null,
)
