package mcdodik.springai.apiai.v1.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PromptDto(
    val id: String?,
    @field:NotBlank val name: String,
    @field:NotNull val type: PromptType,
    @field:NotBlank val template: String,
    val variables: List<String> = emptyList(),
    val version: Int? = null,
    val createdAt: String? = null,
)
