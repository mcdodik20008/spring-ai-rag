package mcdodik.springai.db.entity.user

import java.time.OffsetDateTime

data class ConversationRecord(
    val id: Long? = null,
    val userId: Long,
    val title: String,
    val createdAt: OffsetDateTime? = null,
)
