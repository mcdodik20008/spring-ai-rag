package mcdodik.springai.apiai.v1.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class IngestionRequestDto(
    @field:NotBlank val kbId: String,
    @field:NotNull val sourceType: IngestionSourceType,
    val config: IngestionConfigDto? = IngestionConfigDto(),
    @field:Size(min = 1) val sources: List<IngestionSourceDto>,
)
