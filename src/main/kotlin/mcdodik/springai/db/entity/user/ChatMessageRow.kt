package mcdodik.springai.db.entity.user

data class ChatMessageRow(
    val id: Long? = null,
    val conversationId: Long,
    val messageType: String,
    val content: String,
    val createdAt: java.time.OffsetDateTime? = null,
)
