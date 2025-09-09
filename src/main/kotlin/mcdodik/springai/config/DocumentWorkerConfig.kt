package mcdodik.springai.config

import mcdodik.springai.infrastructure.document.reader.CodeAwareTikaReaderFactory
import mcdodik.springai.infrastructure.document.worker.DocumentWorker
import mcdodik.springai.infrastructure.document.worker.LLMDocumentWorker
import mcdodik.springai.infrastructure.document.worker.MarkdownDocumentWorker
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DocumentWorkerConfig {
    @Bean
    @ConditionalOnProperty(name = ["mcdodik.openrouter.enabled"], havingValue = "true")
    fun aiMarkdownWorker(
        @Qualifier("openRouterChatClient")
        chunkExtractor: ChatClient,
    ): DocumentWorker = LLMDocumentWorker(chunkExtractor)

    @Bean
    @ConditionalOnProperty(name = ["mcdodik.openrouter.enabled"], havingValue = "false", matchIfMissing = true)
    fun classicMarkdownWorker(
        splitter: TokenTextSplitter,
        readerFactory: CodeAwareTikaReaderFactory,
    ): DocumentWorker = MarkdownDocumentWorker(splitter, readerFactory)
}
