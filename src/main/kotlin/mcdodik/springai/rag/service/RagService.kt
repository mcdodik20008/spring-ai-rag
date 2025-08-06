package mcdodik.springai.rag.service

import mcdodik.springai.api.controller.model.CleanRequestParams
import mcdodik.springai.config.Loggable
import mcdodik.springai.db.model.DocumentInfo
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.extension.featAllTextFromObsidianMd
import mcdodik.springai.utils.documentworker.DocumentWorkerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux

@Service
class RagService(
    //@Qualifier("openRouterChatClient")
    private val chat: ChatClient,
    @Qualifier("customPgVectorStore")
    private val ragStore: VectorStore,
    private val documentStore: DocumentInfoMapper,
    private val documentWorkerFactory: DocumentWorkerFactory,
    @Qualifier("openRouterChatClient")
    private val summarizer: ChatClient,
) {

    fun ask(question: String): Flux<String> {
        return chat.prompt().user(question).stream().content() //.call().content() ?: "no answer"
    }

    fun ingest(file: MultipartFile, params: CleanRequestParams) {
        // 1. Делим файл на чанки
        val chunks = documentWorkerFactory.process(file, params)

        // 2. Генерация summary
        val text = file.featAllTextFromObsidianMd()
        val summary = summarizer.prompt()
            .user("Вот текст, который нужно суммировать:\n$text")
            .call()
            .content() ?: "no summary"

        // 3. Сохранение информации о документе
        val documentInfo = DocumentInfo.createFromFileAndCunks(file, chunks.size, summary)
        documentStore.insert(documentInfo)

        // 4. Сохраняем чанки в векторное хранилище
        ragStore.write(chunks)
    }

    companion object : Loggable
}



