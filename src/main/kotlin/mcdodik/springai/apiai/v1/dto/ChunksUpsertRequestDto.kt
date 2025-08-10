package mcdodik.springai.apiai.v1.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ChunksUpsertRequestDto(
    @field:NotBlank val docId: String,
    @field:Size(min = 1, max = 10000) val chunks: List<UpsertChunkDto>,
    val embedIfMissing: Boolean = true,
)
