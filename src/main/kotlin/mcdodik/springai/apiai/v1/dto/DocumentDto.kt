package mcdodik.springai.apiai.v1.dto

data class DocumentDto(
    val id: String,
    val kbId: String,
    val fileName: String,
    val mime: String?,
    val size: Long?,
    val hash: String?,
    val status: String,
    val createdAt: String,
    val summary: String?,
    val tags: List<String>?,
)
