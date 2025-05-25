package mcdodik.springai.rag.services

import mcdodik.springai.config.Loggable
import mcdodik.springai.controller.model.PdfCleanRequest
import mcdodik.springai.rag.db.CustomVectorStore
import mcdodik.springai.rag.formatting.ContextMarkdownFormatter
import mcdodik.springai.utils.documentworker.DocumentWorkerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
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

    fun ask(question: String): String {
        val chunks = vectorStore.search(question)
        logger.debug("Founded chunks: {}", chunks)

        val formattedContext = contextMarkdownFormatter.format(chunks)
        logger.debug("Formatted context: $formattedContext")

        val renderedPrompt = promptTemplate.render(
            mapOf(
                "context" to formattedContext,
                "question" to question
            )
        )
        logger.info("Question: $question rendered: $renderedPrompt")

        val answer = chat.prompt().user(renderedPrompt).call().content() ?: "no answer"
        logger.info("Answer: $answer")
        return answer
    }

    fun ingest(file: MultipartFile, params: PdfCleanRequest) {
        val docs = documentWorkerFactory.process(file, params)

        vectorStore.write(docs)
    }

    companion object : Loggable
}