package mcdodik.springai.db.entity.user

import org.springframework.ai.chat.messages.MessageType
import java.time.OffsetDateTime

data class ChatMessageRecord(
    val id: Long? = null,
    val conversationId: Long,
    val messageType: MessageType,
    val content: String,
    val createdAt: OffsetDateTime? = null,
)
