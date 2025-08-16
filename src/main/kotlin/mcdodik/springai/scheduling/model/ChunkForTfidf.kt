package mcdodik.springai.scheduling.model

import java.time.Instant
import java.util.UUID

data class ChunkForTfidf(
    val id: UUID,
    val text: String,
    val updatedAt: Instant,
)
