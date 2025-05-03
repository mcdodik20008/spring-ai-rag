package mcdodik.springai.openrouter

import org.springframework.ai.chat.messages.AbstractMessage
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.metadata.ChatResponseMetadata
import org.springframework.ai.chat.metadata.DefaultUsage
import org.springframework.ai.chat.metadata.Usage
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
class OpenRouterChatModel(
    private val restTemplate: RestTemplate,
    @Value("\${openrouter.api-key}") private val apiKey: String
) : ChatModel {

    private val endpoint = "https://openrouter.ai/api/v1/chat/completions"

    override fun call(prompt: Prompt): ChatResponse {
        val messages = prompt.instructions.map { msg ->
            val abstract = msg as AbstractMessage
            mapOf(
                "role" to abstract.messageType.name.lowercase(), // user, assistant, system
                "content" to abstract.text
            )
        }

        val requestBody = mapOf(
            "model" to "microsoft/phi-4-reasoning-plus:free",
            "messages" to messages
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }

        val httpEntity = HttpEntity(requestBody, headers)

        val response = restTemplate.exchange(
            endpoint,
            HttpMethod.POST,
            httpEntity,
            Map::class.java
        )

        val raw = response.body ?: error("Empty response")

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
}
