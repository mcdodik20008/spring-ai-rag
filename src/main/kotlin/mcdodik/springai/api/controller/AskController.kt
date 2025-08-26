package mcdodik.springai.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import mcdodik.springai.api.dto.AskRequest
import mcdodik.springai.rag.service.RagService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/ask")
class AskController(
    private val rag: RagService
) {
    @Operation(
        summary = "Задать вопрос RAG",
        description = "Принимает вопрос и возвращает поток строк (чанки ответа). " +
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
                    // Альтернативная документация как NDJSON (если переключишься)
                    Content(
                        mediaType = "application/x-ndjson",
                        array = ArraySchema(schema = Schema(implementation = String::class)),
                    )
                ]
            ),
            ApiResponse(responseCode = "400", description = "Неверный запрос"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE] // чётко фиксируем SSE
    )
    suspend fun ask(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Тело запроса с вопросом",
            content = [Content(schema = Schema(implementation = AskRequest::class))]
        )
        @RequestBody req: AskRequest,
    ): Flux<String> = rag.ask(req.question)
}
