package mcdodik.springai.openrouter.model

data class TokenUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)