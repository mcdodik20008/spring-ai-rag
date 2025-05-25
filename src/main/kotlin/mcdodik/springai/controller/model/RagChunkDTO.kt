package mcdodik.springai.controller.model

import java.util.*

data class RagChunkDTO(
    val id: UUID = UUID.randomUUID(),
    val content: String,
    val summary: String?,
    val embedding: List<Float>,
    val type: String = "text",
    val source: String? = null,
    val chunkIndex: Int = 0
)