package mcdodik.springai.config

import mcdodik.springai.utils.documentworker.DocumentWorker
import mcdodik.springai.utils.documentworker.LLMDocumentWorker
import mcdodik.springai.utils.documentworker.MarkdownDocumentWorker
import mcdodik.springai.utils.reader.CodeAwareTikaReaderFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up document workers based on the application's configuration.
 */
@Configuration
class DocumentWorkerConfig {

    /**
     * Bean definition for the AI-based Markdown document worker.
     * This worker is used when the "openrouter.enabled" property is set to "true".
     *
     * @param chunkExtractor The chat client used to extract and process document chunks.
     * @return An instance of [LLMDocumentWorker] configured with the provided chat client.
     */
    @Bean
    @ConditionalOnProperty(name = ["openrouter.enabled"], havingValue = "true")
    fun aiMarkdownWorker(
        @Qualifier("openRouterChatClient")
        chunkExtractor: ChatClient
    ): DocumentWorker = LLMDocumentWorker(chunkExtractor)

    /**
     * Bean definition for the classic Markdown document worker.
     * This worker is used when the "openrouter.enabled" property is set to "false" or is missing.
     *
     * @param splitter The text splitter used to split document content into manageable chunks.
     * @param readerFactory The factory for creating readers that are aware of code blocks in Markdown.
     * @return An instance of [MarkdownDocumentWorker] configured with the provided splitter and reader factory.
     */
    @Bean
    @ConditionalOnProperty(name = ["openrouter.enabled"], havingValue = "false", matchIfMissing = true)
    fun classicMarkdownWorker(
        splitter: TokenTextSplitter,
        readerFactory: CodeAwareTikaReaderFactory
    ): DocumentWorker = MarkdownDocumentWorker(splitter, readerFactory)

}
