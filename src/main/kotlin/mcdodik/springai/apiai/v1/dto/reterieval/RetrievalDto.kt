package mcdodik.springai.apiai.v1.dto.reterieval

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Positive
import mcdodik.springai.apiai.v1.dto.RerankerDto

data class RetrievalDto(
    val enabled: Boolean = true,
    @field:Positive @field:Max(1000) val topK: Int = 8,
    @field:DecimalMin("0.0") @field:DecimalMax("1.0") val vectorWeight: Double = 0.6,
    @field:DecimalMin("0.0") @field:DecimalMax("1.0") val bm25Weight: Double = 0.4,
    @field:DecimalMin("0.0") @field:DecimalMax("1.0") val threshold: Double = 0.2,
    val similarityMetric: String? = "cosine",
    val filters: Map<String, Any?>? = null,
    val namespace: String? = null,
    val reranker: RerankerDto? = null,
)
