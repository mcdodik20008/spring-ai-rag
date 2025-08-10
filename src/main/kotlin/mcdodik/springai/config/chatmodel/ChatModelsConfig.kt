package mcdodik.springai.config.chatmodel

import mcdodik.springai.advisors.VectorAdvisor
import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.CHUNKING
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.DEFAULT
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.PROMPT_GEN
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.SUMMARY
import mcdodik.springai.openrouter.OpenRouterChat
import mcdodik.springai.rag.api.ContextBuilder
import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.api.SummaryService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.ollama.OllamaEmbeddingModel
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
        advisor: BaseAdvisor,
    ): ChatClient =
        ChatClient
            .builder(chatModel)
            .defaultAdvisors(advisor)
            .build()

    @Bean
    @Qualifier("vectorAdvisor")
    fun vectorAdvisor(
        properties: VectorAdvisorProperties,
        embeddingModel: OllamaEmbeddingModel,
        retriever: Retriever,
        reranker: Reranker,
        contextBuilder: ContextBuilder,
        summaryService: SummaryService,
    ): BaseAdvisor {
        return VectorAdvisor(
            properties = properties,
            embeddingModel = embeddingModel,
            retriever = retriever,
            reranker = reranker,
            contextBuilder = contextBuilder,
            summaryService = summaryService,
        )
    }

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
    ): (LLMTaskType?, String?) -> ChatClient {
        return { taskType, overrideModel ->

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
    }

    enum class LLMTaskType {
        SUMMARY,
        PROMPT_GEN,
        CHUNKING,
        DEFAULT,
    }
}
