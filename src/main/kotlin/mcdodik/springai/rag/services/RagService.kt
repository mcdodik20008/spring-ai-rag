package mcdodik.springai.rag.services

import mcdodik.springai.config.Loggable
import mcdodik.springai.controller.model.CleanRequestParams
import mcdodik.springai.rag.formatting.ContextMarkdownFormatter
import mcdodik.springai.utils.documentworker.DocumentWorkerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import java.security.MessageDigest
import mcdodik.springai.rag.db.DocumentInfo
import mcdodik.springai.rag.db.mybatis.mapper.DocumentInfoMapper

@Service
class RagService(
    //@Qualifier("openRouterChatClient")
    private val chat: ChatClient,
    @Qualifier("customPgVectorStore")
    private val ragStore: VectorStore,
    private val documentStore: DocumentInfoMapper,
    private val documentWorkerFactory: DocumentWorkerFactory,
    private val contextMarkdownFormatter: ContextMarkdownFormatter,
    private val promptTemplate: PromptTemplate,
    //@Qualifier("openRouterChatClient") private val summarizer: ChatClient,
) {

    fun ask(question: String): Flux<String> {
        val chunks = ragStore.similaritySearch(question)
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

        return chat.prompt().user(renderedPrompt).stream().content() //.call().content() ?: "no answer"
    }

    fun ingest(file: MultipartFile, params: CleanRequestParams) {
        val chunks = documentWorkerFactory.process(file, params)
        chunks.forEachIndexed { n, doc -> doc.fetchInfoFromFile(n, file) }

        val documentInfo = DocumentInfo.createFromFileAndCunks(file, chunks.size)
        documentStore.insert(documentInfo)

        ragStore.write(chunks)
    }

    companion object : Loggable
}

private fun Document.fetchInfoFromFile(n: Int, file: MultipartFile) {
    this.metadata["chunk_index"] = n
    this.metadata["file_name"] = file.originalFilename
    this.metadata["extension"] = file.contentType
    this.metadata["hash"] = file.sha256()
}



fun MultipartFile.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream.use {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (it.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

