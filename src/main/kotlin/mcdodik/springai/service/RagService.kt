package mcdodik.springai.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.reader.markdown.MarkdownDocumentReader
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service

@Service
class RagService(
    private val chat: ChatClient,
    private val vectorStore: VectorStore,
) {

    private val splitter: TokenTextSplitter = TokenTextSplitter.builder().withChunkSize(300).build()

    fun ingest(markdown: String) {
        val resource = object : ByteArrayResource(markdown.toByteArray()) {
            override fun getFilename(): String = "inline.md"
        }
        val documentReader = MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig())
        vectorStore.write(
            splitter.apply(documentReader.read())
        )
    }

    fun ask(question: String): String? =
        chat.prompt().user(question).call().content()
}

