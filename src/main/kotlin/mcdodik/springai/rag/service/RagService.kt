package mcdodik.springai.rag.service

import mcdodik.springai.api.dto.CleanRequestParams
import mcdodik.springai.config.Loggable
import mcdodik.springai.db.entity.rag.DocumentInfo
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.extensions.featAllTextFromObsidianMd
import mcdodik.springai.infrastructure.document.worker.DocumentWorkerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux

@Service
class RagService(
    @Qualifier("ollamaChatClient")
    private val chat: ChatClient,
    @Qualifier("customPgVectorStore")
    private val ragStore: VectorStore,
    private val documentStore: DocumentInfoMapper,
    private val documentWorkerFactory: DocumentWorkerFactory,
    @Qualifier("openRouterChatClient")
    private val summarizer: ChatClient,
) {
    fun ask(question: String): Flux<String> {
        return chat
            .prompt("Ответь на русском")
            .user(question)
            .stream()                  // StreamResponseSpec
            .content()                 // Flux<String>
            .onErrorResume { _ ->
                // если стрим упал (тот самый NPE внутри Spring AI) — вернём разовый ответ
                reactor.core.publisher.Mono.just(
                    chat
                        .prompt("Ответь на русском")
                        .user(question)
                        .call()
                        .content()
                        .orEmpty()
                )
            }


    }

    fun ingest(
        file: MultipartFile,
        params: CleanRequestParams,
    ) {
        logger.info("splitting file to chunks ${file.originalFilename}")
        val chunks = documentWorkerFactory.process(file, params)

        logger.info("summarizing file ${file.originalFilename}")
        val text = file.featAllTextFromObsidianMd()
        val summary =
            summarizer.prompt()
                .user("Вот текст, который нужно суммировать:\n$text")
                .call()
                .content() ?: "no summary"

        val documentInfo = DocumentInfo.createFromFileAndCunks(file, chunks.size, summary)
        documentStore.insert(documentInfo)

        ragStore.write(chunks)
    }

    companion object : Loggable
}
