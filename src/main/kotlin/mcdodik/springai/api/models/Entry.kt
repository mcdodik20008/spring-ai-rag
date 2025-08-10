package mcdodik.springai.api.models

import java.time.Instant

data class Entry(val status: Int, val body: ByteArray, val createdAt: Long = Instant.now().epochSecond) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entry

        if (status != other.status) return false
        if (createdAt != other.createdAt) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }
}
