package mcdodik.springai.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun chatClient(chatModel: OllamaChatModel, vectorStore: VectorStore): ChatClient =
        ChatClient.builder(chatModel)
            .defaultSystem("You are a concise Kotlin assistant.")
            .defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
            .build()
}
