package mcdodik.springai.apiai.v1.dto

data class PromptShapingDto(
    val systemPromptId: String? = null,
    val augmentWithContext: Boolean = true,
    val contextFormat: String? = "bulleted",
)
