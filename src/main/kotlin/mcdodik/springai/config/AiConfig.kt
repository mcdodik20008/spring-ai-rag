package mcdodik.springai.config

import mcdodik.springai.openrouter.OpenRouterChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.template.TemplateRenderer
import org.springframework.ai.template.ValidationMode
import org.springframework.ai.template.st.StTemplateRenderer
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
            //.defaultSystem("You are a helpful assistant. Use the following documents to answer the user question:")
            //.defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
            .build()

    @Bean
    @Qualifier("openRouterChatClient")
    fun openRouterChatClient(chatModel: OpenRouterChatModel, vectorStore: VectorStore): ChatClient =
        ChatClient.builder(chatModel)
            //.defaultSystem("You are a helpful assistant. Use the following documents to answer the user question:")
            //.defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
            .build()


    @Bean
    fun tokenTextSplitter(): TokenTextSplitter = TokenTextSplitter(
        512,     // chunkSize (токенов): увеличиваем до 1024 (около 2–3 страниц)
        256,      // minChunkSizeChars: увеличиваем минимальный размер (около 1 абзаца)
        128,      // minChunkLengthToEmbed: избегаем мелких, неинформативных фрагментов
        1000,     // maxNumChunks: ограничиваем разумным числом чанков
        true      // keepSeparator: для сохранения структуры текста
    )

    @Bean
    fun promptTemplate(renderer: TemplateRenderer): PromptTemplate {
        val template = PromptTemplate.builder().template(
            """
        Ты — опытный помощник по программированию.
        Используй только данные из контекста, не выдумывай.
        Контекст:
        <context>

        Вопрос: <question>

        Ответ:
        """.trimIndent()
        )
            .renderer(renderer)
            .build()
        return template
    }

    @Bean
    fun templateRenderer(): TemplateRenderer {
        return StTemplateRenderer.builder()
            .endDelimiterToken('>')
            .startDelimiterToken('<')
            .validationMode(ValidationMode.NONE)
            .build()
    }

}
