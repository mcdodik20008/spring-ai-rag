package mcdodik.springai.db.entity.chat

import org.springframework.ai.chat.messages.MessageType
import java.time.Instant

data class ChatMessage(
    val id: Long? = null,
    val conversationId: String,
    val messageType: MessageType,
    val content: String,
    val createdAt: Instant = Instant.now(),
)
