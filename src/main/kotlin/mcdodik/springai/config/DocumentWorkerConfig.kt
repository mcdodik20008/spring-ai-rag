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

@Configuration
class DocumentWorkerConfig {

    @Configuration
    class DocumentWorkerConfig {

        @Bean
        @ConditionalOnProperty(name = ["openrouter.enabled"], havingValue = "true")
        fun aiMarkdownWorker(
            @Qualifier("openRouterChatClient")
            chunkExtractor: ChatClient
        ): DocumentWorker = LLMDocumentWorker(chunkExtractor)

        @Bean
        @ConditionalOnProperty(name = ["openrouter.enabled"], havingValue = "false", matchIfMissing = true)
        fun classicMarkdownWorker(
            splitter: TokenTextSplitter,
            readerFactory: CodeAwareTikaReaderFactory
        ): DocumentWorker = MarkdownDocumentWorker(splitter, readerFactory)
    }


}
