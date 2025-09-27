package mcdodik.springai.db.entity.user

import java.time.OffsetDateTime

data class UserRecord(
    val id: Long? = null,
    val login: String,
    val createdAt: OffsetDateTime? = null,
)
