package mcdodik.springai.api.dto.user

import org.springframework.ai.chat.messages.MessageType

data class ChatMessageResponse(
    val id: Long,
    val conversationId: Long,
    val messageType: MessageType,
    val content: String,
)
