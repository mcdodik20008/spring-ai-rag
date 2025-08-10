package mcdodik.springai.apiai.v1.dto

import jakarta.validation.constraints.NotBlank

data class IngestionSourceDto(
    val url: String? = null,
    val raw: RawDocDto? = null,
) {
    data class RawDocDto(
        @field:NotBlank val fileName: String,
        @field:NotBlank val content: String,
    )
}
