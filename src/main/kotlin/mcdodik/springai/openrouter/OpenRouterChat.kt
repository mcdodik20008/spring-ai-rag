package mcdodik.springai.openrouter

import mcdodik.springai.config.Loggable
import mcdodik.springai.openrouter.dto.OpenRouterResponse
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.metadata.ChatResponseMetadata
import org.springframework.ai.chat.metadata.DefaultUsage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

class OpenRouterChat(
    private val model: String,
    private val webClient: WebClient,
    private val apiKey: String,
    private val temperature: Double = 0.2,
    private val topP: Double = 0.9,
    private val maxTokens: Int = 1000,
) : ChatModel {
    private val endpoint = "https://openrouter.ai/api/v1/chat/completions"

    override fun call(prompt: Prompt): ChatResponse {
        // 1) соберём сообщения
        val messages =
            prompt.instructions.mapNotNull { msg ->
                when (msg) {
                    is SystemMessage -> mapOf("role" to "system", "content" to msg.text)
                    is UserMessage -> mapOf("role" to "user", "content" to msg.text)
                    is AssistantMessage -> mapOf("role" to "assistant", "content" to msg.text)
                    else -> null
                }
            }

        // 2) тело запроса
        val requestBody =
            mapOf(
                "model" to model,
                "messages" to messages,
                "temperature" to temperature,
                "top_p" to topP,
                "max_tokens" to maxTokens,
                "stream" to false,
            )

        // 3) вызов
        val resp: OpenRouterResponse =
            try {
                webClient
                    .post()
                    .uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers {
                        it.setBearerAuth(apiKey) // Authorization: Bearer ...
                        it.accept = listOf(MediaType.APPLICATION_JSON)
                    }.bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(OpenRouterResponse::class.java)
                    .block() ?: error("Empty response")
            } catch (e: WebClientResponseException) {
                logger.error("OpenRouter error {}: {}", e.statusCode, e.responseBodyAsString)
                throw e
            }

        // 4) разбор
        val content =
            resp.choices
                .firstOrNull()
                ?.message
                ?.content
                ?: error("Missing content in OpenRouter response")

        val generation = Generation(AssistantMessage(content))

        val metadata =
            ChatResponseMetadata
                .builder()
                .id(resp.id)
                .model(resp.model)
                .usage(
                    DefaultUsage(
                        resp.usage?.promptTokens ?: 0,
                        resp.usage?.completionTokens ?: 0,
                        resp.usage?.totalTokens ?: 0,
                    ),
                ).build()

        return ChatResponse
            .builder()
            .generations(listOf(generation))
            .metadata(metadata)
            .build()
    }

    companion object : Loggable
}
