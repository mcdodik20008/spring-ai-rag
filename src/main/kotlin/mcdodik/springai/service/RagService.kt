package mcdodik.springai.service

import mcdodik.springai.utils.document.DocumentWorkerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class RagService(
    //@Qualifier("openRouterChatClient")
    private val chat: ChatClient,
    private val vectorStore: VectorStore,
    private val documentWorkerFactory: DocumentWorkerFactory,
) {

    private val splitter: TokenTextSplitter = TokenTextSplitter.builder().withChunkSize(300).build()

    fun ask(question: String): String? {
        return chat.prompt().user(question).call().content()
    }

    fun ingest(file: MultipartFile) {
        val docs = documentWorkerFactory.process(file)
        vectorStore.write(splitter.apply(docs))
    }
}
