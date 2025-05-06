package mcdodik.springai.rag.db

import java.time.LocalDateTime
import java.util.*


data class RagChunk(
    val id: UUID = UUID.randomUUID(),
    val content: String,
    val embedding: List<Float>,
    val type: String,
    val source: String?,
    val chunkIndex: Int?,
    val createdAt: LocalDateTime?,
    val summary: String?
)
