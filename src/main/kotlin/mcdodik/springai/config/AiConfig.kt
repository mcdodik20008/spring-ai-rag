package mcdodik.springai.config

import mcdodik.springai.openrouter.OpenRouterChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AiConfig {

    @Bean
    @Primary
    fun ollamaChatClient(chatModel: OllamaChatModel, vectorStore: VectorStore): ChatClient =
        ChatClient.builder(chatModel)
            .defaultSystem("You are a helpful assistant. Use the following documents to answer the user question:")
            .defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
            .build()

    @Bean
    @Qualifier("openRouterChatClient")
    fun openRouterChatClient(chatModel: OpenRouterChatModel, vectorStore: VectorStore): ChatClient =
        ChatClient.builder(chatModel)
            .defaultSystem("You are a helpful assistant. Use the following documents to answer the user question:")
            .defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
            .build()

    @Bean
    fun tokenTextSplitter(): TokenTextSplitter =
        // default: chunkSize=800 tokens, minChars=350, minLenToEmbed=5, maxChunks=10_000, keepSeparator=true
        TokenTextSplitter(
            2024,    // целевой размер чанка в токенах
            256,     // минимальный размер чанка в символах
            16,      // минимальная длина чанка для встраивания
            50_000,   // максимум чанков из одного документа
            true     // сохранять ли разделители (переводы строк)
        )
}
