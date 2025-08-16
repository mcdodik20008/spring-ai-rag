package mcdodik.springai.scheduling.model

import java.util.*

data class ChunkTfidfUpdate(
    val id: UUID,
    val tfidf: Map<String, Double>,
    val tfidfNorm: Double,
)
