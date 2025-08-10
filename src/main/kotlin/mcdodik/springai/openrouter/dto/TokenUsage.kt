package mcdodik.springai.openrouter.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenUsage(
    @JsonProperty("prompt_tokens")
    val promptTokens: Int,
    @JsonProperty("completion_tokens")
    val completionTokens: Int,
    @JsonProperty("total_tokens")
    val totalTokens: Int,
)
