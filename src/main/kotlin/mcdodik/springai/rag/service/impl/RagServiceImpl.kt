package mcdodik.springai.rag.service.impl

import mcdodik.springai.api.dto.ingest.CleanRequestParams
import mcdodik.springai.config.Loggable
import mcdodik.springai.db.entity.rag.DocumentInfo
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.extensions.featAllTextFromObsidianMd
import mcdodik.springai.infrastructure.document.worker.DocumentWorker
import mcdodik.springai.rag.service.api.RagService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow

@Service
class RagServiceImpl(
    @Qualifier("ollamaChatClient")
    private val chat: ChatClient,
    @Qualifier("customPgVectorStore")
    private val ragStore: VectorStore,
    private val documentStore: DocumentInfoMapper,
    private val worker: DocumentWorker,
    @Qualifier("openRouterChatClient")
    private val summarizer: ChatClient,
) : RagService {
    override fun ask(question: String): Flux<String> {
        val req = buildDirectAnswerPrompt(chat, question)
        return req
            .stream()
            .content()
            .onErrorResume { e ->
                logger.warn("ask(): stream failed, fallback to call(): {}", e.toString())
                Mono
                    .fromCallable {
                        req.call().content().orEmpty()
                    }.subscribeOn(
                        reactor.core.scheduler.Schedulers
                            .boundedElastic(),
                    )
            }.doOnSubscribe { logger.debug("ask(): q='{}'", question.take(120)) }
            .doOnNext { logger.trace("ask(): chunk={}", it.length) }
            .doOnComplete { logger.debug("ask(): completed") }
    }

    override fun askFlow(question: String): Flow<String> = ask(question).asFlow()

    private fun buildDirectAnswerPrompt(
        chat: ChatClient,
        question: String,
    ): ChatClient.ChatClientRequestSpec =
        chat
            .prompt()
            .system("Отвечай кратко и по делу. Русский язык. Не раскрывай ход рассуждений.")
            .user(question)

    override fun ingest(
        file: MultipartFile,
        params: CleanRequestParams,
    ) {
        logger.info("splitting file to chunks ${file.originalFilename}")
        val chunks = worker.process(file, params)

        logger.info("summarizing file ${file.originalFilename}")
        val text = file.featAllTextFromObsidianMd()
        val summary =
            summarizer
                .prompt()
                .user("Вот текст, который нужно суммировать:\n$text")
                .call()
                .content() ?: "no summary"

        val documentInfo = DocumentInfo.createFromFileAndCunks(file, chunks.size, summary)
        documentStore.insert(documentInfo)

        ragStore.write(chunks)
    }

    companion object : Loggable
}
