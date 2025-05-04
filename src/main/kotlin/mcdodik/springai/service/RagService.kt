package mcdodik.springai.service

import mcdodik.springai.db.CustomVectorStore
import mcdodik.springai.prerag.ContextMarkdownFormatter
import mcdodik.springai.utils.document.DocumentWorkerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class RagService(
    //@Qualifier("openRouterChatClient")
    private val chat: ChatClient,
    private val vectorStore: CustomVectorStore,
    private val documentWorkerFactory: DocumentWorkerFactory,
    private val contextMarkdownFormatter: ContextMarkdownFormatter,
    private val promptTemplate: PromptTemplate,
) {

    fun ask2(question: String): String? {
        return chat.prompt().user(question).call().content()
    }

    fun ask(question: String): String {
        val chunks = vectorStore.search(question)
        val formattedContext = contextMarkdownFormatter.format(chunks)

        val renderedPrompt = promptTemplate.render(
            mapOf(
                "context" to formattedContext,
                "question" to question
            )
        )
        println("rendered prompt: $renderedPrompt")
        return chat.prompt().user(renderedPrompt).call().content() ?: "no answer"
    }

    fun ingest(file: MultipartFile) {
        val docs = documentWorkerFactory.process(file)
        vectorStore.write(docs)
    }
}
