package mcdodik.springai.scheduling.model

import java.util.UUID

data class ChunkForDedup(
    val id: UUID,
    val tfidf: Map<String, Double>,
    val tfidfNorm: Double?,
)
