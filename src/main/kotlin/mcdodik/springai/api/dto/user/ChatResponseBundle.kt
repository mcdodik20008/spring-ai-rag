package mcdodik.springai.api.dto.user

data class ChatResponseBundle(
    val conversationId: Long,
    val userMessage: ChatMessageResponse,
    val aiMessage: ChatMessageResponse,
)
