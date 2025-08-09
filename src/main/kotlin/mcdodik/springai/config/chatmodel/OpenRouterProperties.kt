package mcdodik.springai.config.chatmodel

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mcdodik.openrouter")
data class OpenRouterProperties(
    val apiKey: String,
    val temperature: Double = 0.2,
    val topP: Double = 0.9,
    val maxTokens: Int = 1000,
    val models: Models = Models(),
) {
    data class Models(
        val default: String = "deepseek/deepseek-r1:free",
        val summary: String? = null,
        val promptGen: String? = null,
        val chunking: String? = null,
    )
}
