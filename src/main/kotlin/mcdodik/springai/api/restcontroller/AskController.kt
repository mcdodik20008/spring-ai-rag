package mcdodik.springai.api.restcontroller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Provider
import mcdodik.springai.api.dto.ask.AskRequest
import mcdodik.springai.config.Loggable
import mcdodik.springai.rag.service.api.RagService
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.WebSession
import reactor.core.publisher.Flux
import kotlinx.coroutines.reactive.awaitSingle

@RestController
@RequestMapping("/api")
class AskController(
    private val rag: RagService,
    private val chatMemoryProvider: Provider<ChatMemory>,
) : Loggable {
    @Operation(
        summary = "Задать вопрос RAG",
        description =
            "Принимает вопрос и возвращает поток строк (чанки ответа). " +
                "Рекомендуемый медиатип — text/event-stream (SSE).",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Стрим чанков текста",
                content = [
                    Content(
                        mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                        array = ArraySchema(schema = Schema(implementation = String::class)),
                    ),
                ],
            ),
            ApiResponse(responseCode = "400", description = "Неверный запрос"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка"),
        ],
    )
    @PostMapping(
        "/ask",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    suspend fun ask(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Тело запроса с вопросом",
            content = [Content(schema = Schema(implementation = AskRequest::class))],
        )
        @RequestBody req: AskRequest,
        session: WebSession,
    ): Flux<String> {
        val chatMemory =
            session.attributes.getOrPut(CHAT_MEMORY_KEY) {
                chatMemoryProvider.get()
            } as ChatMemory
        return rag.ask(req.question, chatMemory)
    }

    @Operation(
        summary = "Задать вопрос RAG",
        description = "Принимает вопрос и возвращает готовый ответ одной строкой.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Текст ответа",
                content = [
                    Content(
                        mediaType = MediaType.TEXT_PLAIN_VALUE,
                        schema = Schema(implementation = String::class),
                    ),
                ],
            ),
            ApiResponse(responseCode = "400", description = "Неверный запрос"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка"),
        ],
    )
    @PostMapping(
        "/ask-text",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    suspend fun askAwait(
        @RequestBody req: AskRequest,
        session: WebSession,
    ): String {
        val chatMemory =
            session.attributes.getOrPut(CHAT_MEMORY_KEY) {
                chatMemoryProvider.get()
            } as ChatMemory
        logger.info("session creation time: ${session.creationTime}")
        return rag
            .ask(req.question, chatMemory)
            .collectList()
            .map { it.joinToString(separator = "") }
            .awaitSingle()
    }

    @PostMapping(
        "/ask-text-any",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend fun askAwaitAny(
        @RequestBody req: AskRequest,
        session: WebSession,
    ): AskTextResponse {
        val chatMemory =
            session.attributes.getOrPut(CHAT_MEMORY_KEY) {
                chatMemoryProvider.get()
            } as ChatMemory
        val response =
            rag
                .ask(req.question, chatMemory)
                .collectList()
                .map { it.joinToString("") }
                .awaitSingle()
        return AskTextResponse(
            response,
        )
    }

    companion object {
        private const val CHAT_MEMORY_KEY = "chat_memory_session_key"
    }
}

data class AskTextResponse(
    val answer: String,
)
