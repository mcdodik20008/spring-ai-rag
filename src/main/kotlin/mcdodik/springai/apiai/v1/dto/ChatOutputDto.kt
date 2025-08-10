package mcdodik.springai.apiai.v1.dto

data class ChatOutputDto(
    val role: Role = Role.ASSISTANT,
    val content: String,
)
