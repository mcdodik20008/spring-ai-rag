package mcdodik.springai.scheduling.model

data class CombinedStatus(
    val results: List<OpStatus>,
    val totalDurationMs: Long,
)
