package mcdodik.springai.config.chatmodel

import mcdodik.springai.advisors.VectorAdvisor
import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.CHUNKING
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.DEFAULT
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.PROMPT_GEN
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType.SUMMARY
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.openrouter.OpenRouterChat
import mcdodik.springai.rag.api.ContextBuilder
import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.api.SummaryService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

@Configuration
class ChatModelsConfig {

    @Autowired
    @Qualifier("customPgVectorStore")
    var vectorStore: VectorStore? = null

    @Bean
    @Primary
    @Qualifier("ollamaChatClient")
    fun ollamaChatClient(
        chatModel: OllamaChatModel,
        properties: VectorAdvisorProperties,
        embeddingModel: OllamaEmbeddingModel,
        retriever: Retriever,
        reranker: Reranker,
        contextBuilder: ContextBuilder,
        summaryService: SummaryService
    ): ChatClient =
        ChatClient.builder(chatModel)
            //.defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore!!).build())
            .defaultAdvisors(
                VectorAdvisor(
                    properties = properties,
                    embeddingModel = embeddingModel,
                    retriever = retriever,
                    reranker = reranker,
                    contextBuilder = contextBuilder,
                    summaryService = summaryService
                )
            )
            .build()


    @Bean
    @Qualifier("openRouterChatClient")
    fun openRouterChatClient(props: OpenRouterProperties, restTemplate: RestTemplate): ChatClient {
        val model = OpenRouterChat(
            restTemplate = restTemplate,
            apiKey = props.apiKey,
            model = props.models.default,
            temperature = props.temperature,
            topP = props.topP,
            maxTokens = props.maxTokens
        )

        return ChatClient.builder(model).build()
    }


    @Bean
    fun dynamicOpenRouterChatClient(
        props: OpenRouterProperties,
        restTemplate: RestTemplate
    ): (LLMTaskType?, String?) -> ChatClient {
        return { taskType, overrideModel ->

            val modelName = overrideModel
                ?: when (taskType) {
                    SUMMARY -> props.models.summary
                    PROMPT_GEN -> props.models.promptGen
                    CHUNKING -> props.models.chunking
                    DEFAULT -> props.models.default
                    null -> props.models.default
                }
                ?: props.models.default

            val model = OpenRouterChat(
                restTemplate = restTemplate,
                apiKey = props.apiKey,
                model = modelName,
                temperature = props.temperature,
                topP = props.topP,
                maxTokens = props.maxTokens
            )

            ChatClient.builder(model).build()
        }
    }

    enum class LLMTaskType {
        SUMMARY, PROMPT_GEN, CHUNKING, DEFAULT
    }
}