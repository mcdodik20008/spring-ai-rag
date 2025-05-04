package mcdodik.springai.db

import java.time.LocalDateTime
import java.util.UUID

data class RagChunk(
    val id: UUID = UUID.randomUUID(),
    val content: String,
    val embedding: List<Float>,
    val type: String,
    val source: String?,
    val chunkIndex: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
