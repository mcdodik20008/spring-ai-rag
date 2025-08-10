package mcdodik.springai.apiai.v1.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ChatMessageDto(
    @field:NotNull val role: Role,
    @field:NotBlank val content: String,
)
