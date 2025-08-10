package mcdodik.springai.apiai.v1.dto

data class PageDto<T>(
    val items: List<T>,
    val total: Long,
    val limit: Int,
    val offset: Int,
)
