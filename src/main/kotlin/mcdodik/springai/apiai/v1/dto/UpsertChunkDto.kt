package mcdodik.springai.apiai.v1.dto

import jakarta.validation.constraints.NotBlank

data class UpsertChunkDto(
    val idx: Int,
    @field:NotBlank val content: String,
    val meta: Map<String, Any?>? = null,
    val embedding: List<Double>? = null,
)
