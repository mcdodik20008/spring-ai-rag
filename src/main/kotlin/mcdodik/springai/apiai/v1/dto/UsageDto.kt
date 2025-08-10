package mcdodik.springai.apiai.v1.dto

data class UsageDto(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
)
