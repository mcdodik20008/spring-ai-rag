package mcdodik.springai.api.dto.user

data class CreateMessageRequest(
    val userId: Long,
    val conversationId: Long,
    val messageType: String,
    val content: String,
)
