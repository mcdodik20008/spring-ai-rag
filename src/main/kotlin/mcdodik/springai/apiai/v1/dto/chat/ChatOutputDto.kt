package mcdodik.springai.apiai.v1.dto.chat

import mcdodik.springai.apiai.v1.dto.Role

data class ChatOutputDto(
    val role: Role = Role.ASSISTANT,
    val content: String,
)
