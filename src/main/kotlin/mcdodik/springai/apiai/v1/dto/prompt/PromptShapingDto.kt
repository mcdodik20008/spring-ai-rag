package mcdodik.springai.apiai.v1.dto.prompt

data class PromptShapingDto(
    val systemPromptId: String? = null,
    val augmentWithContext: Boolean = true,
    val contextFormat: String? = "bulleted",
)
