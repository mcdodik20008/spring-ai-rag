package mcdodik.springai.openrouter

import mcdodik.springai.config.Loggable
import mcdodik.springai.openrouter.model.OpenRouterResponse
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.metadata.ChatResponseMetadata
import org.springframework.ai.chat.metadata.DefaultUsage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class OpenRouterChat(
    private val restTemplate: RestTemplate,
    @Value("\${openrouter.api-key}") private val apiKey: String,
    @Value("\${openrouter.model}") private val model: String,
    @Value("\${openrouter.temperature}") private val temperature: String?,
    @Value("\${openrouter.top-p}") private val topP: String?,
    @Value("\${openrouter.max-tokens}") private val maxTokens: String?
) : ChatModel {
    private val endpoint = "https://openrouter.ai/api/v1/chat/completions"

    override fun call(prompt: Prompt): ChatResponse {
        val messages = prompt.instructions.mapNotNull { msg ->
            when (msg) {
                is SystemMessage -> mapOf("role" to "system", "content" to msg.text)
                is UserMessage -> mapOf("role" to "user", "content" to msg.text)
                is AssistantMessage -> mapOf("role" to "assistant", "content" to msg.text)
                else -> null
            }
        }

        val requestBody = mutableMapOf<String, Any>(
            "model" to model,
            "messages" to messages,
            "temperature" to (temperature?.toDoubleOrNull() ?: 0.2),
            "top_p" to (topP?.toDoubleOrNull() ?: 0.95),
            "max_tokens" to (maxTokens?.toIntOrNull() ?: 1000),
            "stream" to false
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }
        logger.debug("Request body: {}", requestBody)

        val httpEntity = HttpEntity(requestBody, headers)
        logger.debug("Http entity: {}", httpEntity)

        val response = restTemplate.exchange(
            endpoint,
            HttpMethod.POST,
            httpEntity,
            OpenRouterResponse::class.java
        )

        val body = response.body ?: error("Empty response")
        logger.debug("OpenRouter response: {}", body)

        val content = body.choices.firstOrNull()?.message?.content ?: error("Missing content")
        val assistantMessage = AssistantMessage(content)
        val generation = Generation(assistantMessage)

        return ChatResponse.builder()
            .generations(listOf(generation))
            .metadata(
                ChatResponseMetadata.builder()
                    .id(body.id)
                    .model(body.model)
                    .usage(
                        DefaultUsage(
                            body.usage?.prompt_tokens ?: 0,
                            body.usage?.completion_tokens ?: 0,
                            body.usage?.total_tokens ?: 0
                        )
                    )
                    .build()
            )
            .build()
    }

    companion object : Loggable
}
