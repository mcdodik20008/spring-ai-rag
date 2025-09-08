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
import reactor.core.publisher.Mono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext

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
            .stream()
            .content()
            .onErrorResume { e ->
                logger.error("RagService.ask: stream error", e)
                Mono.fromCallable {
                    chat
                        .prompt("Ответь на русском")
                        .user(question)
                        .call() // синхронный
                        .content()
                        .orEmpty()
                }
                    .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
            }
            .doOnSubscribe { logger.debug("RagService.ask: subscribe question='{}'", question.take(80)) }
            .doOnNext { chunk -> logger.trace("RagService.ask: chunk[{}]", chunk.length) }
            .doOnError { t -> logger.error("RagService.ask: stream error", t) }
            .doOnComplete { logger.debug("RagService.ask: completed") }
    }

    fun askFlow(question: String): Flow<String> {
        val flux =
            chat
                .prompt("Ответь на русском")
                .user(question)
                .stream()
                .content() // Flux<String>

        return flux.asFlow()
            .catch { e ->
                logger.warn("RagService.askFlow stream error, fallback to call(): {}", e.message)
                // синхронный fallback на IO-пуле
                val oneShot =
                    withContext(Dispatchers.IO) {
                        chat.prompt("Ответь на русском").user(question).call().content().orEmpty()
                    }
                emit(oneShot)
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
