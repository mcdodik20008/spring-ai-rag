package mcdodik.springai.scheduling.model

import java.util.*

data class ChunkForDedup(
    val id: UUID,
    val tfidf: Map<String, Double>,
    val tfidfNorm: Double,
)
