package mcdodik.springai.apiai.v1.dto.chat

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import mcdodik.springai.apiai.v1.dto.Role

data class ChatMessageDto(
    @field:NotNull val role: Role,
    @field:NotBlank val content: String,
)
