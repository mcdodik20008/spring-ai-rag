package mcdodik.springai.apiai.v1.dto.reterieval

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

data class RetrievedItemDto(
    val chunkId: String,
    @field:DecimalMin("0.0") @field:DecimalMax("1.0") val score: Double,
    val docId: String,
    val highlights: List<String>? = null,
)
