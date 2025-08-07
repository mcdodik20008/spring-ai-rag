package mcdodik.springai.config

import mcdodik.springai.openrouter.OpenRouterChat
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ChatModelsConfig {

    @Autowired
    @Qualifier("customPgVectorStore")
    var vectorStore: VectorStore? = null

    @Bean
    @Primary
    @Qualifier("ollamaChatClient")
    fun ollamaChatClient(chatModel: OllamaChatModel): ChatClient =
        ChatClient.builder(chatModel)
            .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore!!).build())
            .build()


    @Bean
    @Qualifier("openRouterChatClient")
    fun openRouterChatClient(chatModel: OpenRouterChat): ChatClient =
        ChatClient.builder(chatModel).build()


}