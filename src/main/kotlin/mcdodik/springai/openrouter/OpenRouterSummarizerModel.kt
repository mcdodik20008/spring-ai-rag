package mcdodik.springai.openrouter

import mcdodik.springai.config.Loggable
import org.springframework.ai.chat.messages.AbstractMessage
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.metadata.ChatResponseMetadata
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
class OpenRouterSummarizerModel(
    private val restTemplate: RestTemplate,
    @Value("\${openrouter.api-key}") private val apiKey: String,
    @Value("\${openrouter.model}") private val model: String
) : ChatModel {

    private val endpoint = "https://openrouter.ai/api/v1/chat/completions"

    override fun call(prompt: Prompt): ChatResponse {
        val messages = prompt.instructions.map { msg ->
            val abstract = msg as AbstractMessage
            mapOf(
                "role" to abstract.messageType.name.lowercase(), // user, assistant, system
                "content" to "Сделай краткое резюме следующего текста на русском языке:\n" + abstract.text
            )
        }

        val requestBody = mapOf(
            "model" to model,
            "messages" to messages
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }

        val httpEntity = HttpEntity(requestBody, headers)
        logger.debug("Http entity: {}", httpEntity)

        val response = restTemplate.exchange(
            endpoint,
            HttpMethod.POST,
            httpEntity,
            Map::class.java
        )

        val raw = response.body ?: error("Empty response")
        logger.debug("Raw response: {}", raw)

        val content = (((raw["choices"] as? List<*>)?.firstOrNull()
                as? Map<*, *>)?.get("message") as? Map<*, *>)?.get("content") as? String
            ?: error("Missing content")

        val assistantMessage = AssistantMessage(content)
        val generation = Generation(assistantMessage)

        return ChatResponse.builder()
            .generations(listOf(generation))
            .metadata(
                ChatResponseMetadata.builder()
                    .id(raw["id"] as String?)
                    .model(raw["model"] as String?)
                    // .usage(DefaultUsage.fromJson())
                    .build()
            )
            .build()
    }

    companion object : Loggable
}
