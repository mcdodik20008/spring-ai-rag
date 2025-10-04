package mcdodik.springai.api.config

import mcdodik.springai.advisors.PostRequestLoggerAdvisor
import mcdodik.springai.api.config.ChatModelsConfig.LLMTaskType.CHUNKING
import mcdodik.springai.api.config.ChatModelsConfig.LLMTaskType.DEFAULT
import mcdodik.springai.api.config.ChatModelsConfig.LLMTaskType.PROMPT_GEN
import mcdodik.springai.api.config.ChatModelsConfig.LLMTaskType.SUMMARY
import mcdodik.springai.openrouter.OpenRouterChat
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ChatModelsConfig {
    @Bean
    @Primary
    @Qualifier("ollamaChatClient")
    fun ollamaChatClient(
        chatModel: OpenAiChatModel,
        @Qualifier("hybridAdvisor")
        hybridAdvisor: BaseAdvisor,
        @Qualifier("russianAdvisor")
        russianAdvisor: BaseAdvisor,
        @Qualifier("postRequestAdvisor")
        postRequestLoggerAdvisor: PostRequestLoggerAdvisor,
        memory: MessageWindowChatMemory,
    ): ChatClient =
        ChatClient
            .builder(chatModel)
            .defaultAdvisors(hybridAdvisor)
            .defaultAdvisors(russianAdvisor)
            .defaultAdvisors(postRequestLoggerAdvisor)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
            .build()

    @Bean
    fun chatMemory(): MessageWindowChatMemory =
        MessageWindowChatMemory
            .builder()
            .maxMessages(20)
            .build()

    @Bean
    @Qualifier("openRouterChatClient")
    fun openRouterChatClient(
        props: OpenRouterProperties,
        webClient: WebClient,
    ): ChatClient {
        val model =
            OpenRouterChat(
                webClient = webClient,
                apiKey = props.apiKey,
                model = props.models.default,
                temperature = props.temperature,
                topP = props.topP,
                maxTokens = props.maxTokens,
            )

        return ChatClient.builder(model).build()
    }

    @Bean
    fun dynamicOpenRouterChatClient(
        props: OpenRouterProperties,
        webClient: WebClient,
    ): (LLMTaskType?, String?) -> ChatClient =
        { taskType, overrideModel ->

            val modelName =
                overrideModel ?: when (taskType) {
                    SUMMARY -> props.models.summary
                    PROMPT_GEN -> props.models.promptGen
                    CHUNKING -> props.models.chunking
                    DEFAULT -> props.models.default
                    null -> props.models.default
                } ?: props.models.default

            val model =
                OpenRouterChat(
                    webClient = webClient,
                    apiKey = props.apiKey,
                    model = modelName,
                    temperature = props.temperature,
                    topP = props.topP,
                    maxTokens = props.maxTokens,
                )

            ChatClient.builder(model).build()
        }

    enum class LLMTaskType {
        SUMMARY,
        PROMPT_GEN,
        CHUNKING,
        DEFAULT,
    }
}
