package mcdodik.springai.api.restcontroller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mcdodik.springai.api.dto.ask.AskRequest
import mcdodik.springai.rag.service.api.RagService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import kotlinx.coroutines.reactive.awaitSingle

@RestController
@RequestMapping("/api")
class AskController(
    private val rag: RagService,
) {
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
    ): Flux<String> = rag.ask(req.question)

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
    ): String =
        rag
            .ask(req.question)
            .collectList()
            .map { it.joinToString(separator = "") }
            .awaitSingle()

    @PostMapping(
        "/ask-text-any",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend fun askAwaitAny(
        @RequestBody req: AskRequest,
    ): AskTextResponse =
        AskTextResponse(
            rag
                .ask(req.question)
                .collectList()
                .map { it.joinToString("") }
                .awaitSingle(),
        )
}

data class AskTextResponse(
    val answer: String,
)
