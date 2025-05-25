package mcdodik.springai.config

import mcdodik.springai.openrouter.OpenRouterSummarizerModel
import org.springframework.ai.chat.client.ChatClient
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
    fun openRouterChatClient(chatModel: OpenRouterSummarizerModel, vectorStore: VectorStore): ChatClient =
        ChatClient.builder(chatModel)
            //.defaultSystem("You are a helpful assistant. Use the following documents to answer the user question:")
            //.defaultAdvisors(QuestionAnswerAdvisor(vectorStore))
            .build()


    @Bean
    fun tokenTextSplitter(): TokenTextSplitter = TokenTextSplitter(
        1000,     // chunkSize
        256,      // minChunkSizeChars
        128,      // minChunkLengthToEmbed
        1000,     // maxNumChunks
        true      // keepSeparator
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
