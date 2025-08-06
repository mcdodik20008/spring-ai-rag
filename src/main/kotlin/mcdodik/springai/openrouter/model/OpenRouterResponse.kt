package mcdodik.springai.openrouter.model

data class OpenRouterResponse(
    val id: String,
    val model: String,
    val usage: TokenUsage?,
    val choices: List<Choice>
)

