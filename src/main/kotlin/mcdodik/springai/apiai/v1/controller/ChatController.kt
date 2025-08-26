package mcdodik.springai.apiai.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mcdodik.springai.apiai.v1.dto.chat.ChatRequestDto
import mcdodik.springai.apiai.v1.dto.chat.ChatResponseDto
import mcdodik.springai.apiai.v1.serivces.ChatService
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlinx.coroutines.flow.Flow

@Tag(name = "Chat", description = "Синхронная и стриминговая генерация ответов")
@Validated
@RestController
@RequestMapping("/v1/chat")
class ChatController(
    private val chatService: ChatService,
) {
    @Operation(
        summary = "Синхронный completion",
        description = "Генерирует ответ на основе истории сообщений и опций инференса."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешный ответ",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ChatResponseDto::class),
                    examples = [ExampleObject(
                        name = "completionSuccess",
                        value = """
                        {
                          "runId": "2b4d7aa1-7b7b-4c77-9b84-0d8c2b2e3a11",
                          "sessionId": "sess-123",
                          "output": { "messages": [ { "role": "assistant", "content": "Привет! Чем помочь?" } ] },
                          "retrievalDiagnostics": { "chunksUsed": 3, "latencyMsRag": 120 },
                          "usage": { "promptTokens": 120, "completionTokens": 85, "totalTokens": 205 },
                          "latencyMs": 410
                        }
                        """
                    )]
                )]
            ),
            ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "429", description = "Лимит запросов превышен (Rate limit)"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервиса")
        ]
    )
    @PostMapping(
        "/completions",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    suspend fun complete(
        @RequestBody(
            required = true,
            description = "Запрос на генерацию ответа",
            content = [Content(
                schema = Schema(implementation = ChatRequestDto::class),
                examples = [ExampleObject(
                    name = "completionRequest",
                    value = """
                    {
                      "kbId": "kb-legal-ru",
                      "sessionId": "sess-123",
                      "messages": [
                        {"role": "user", "content": "Суммируй договор в 5 пунктах"}
                      ],
                      "retrieval": { "topK": 4, "minScore": 0.6 },
                      "promptShaping": { "system": "Ты помощник-юрист." },
                      "model": "gpt-4o-mini",
                      "temperature": 0.2,
                      "maxTokens": 800
                    }
                    """
                )]
            )]
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody
        req: ChatRequestDto,
    ): ChatResponseDto = chatService.complete(req)

    @Operation(
        summary = "Стриминговый completion (SSE)",
        description = "Возвращает поток событий SSE с частями ответа. Каждое событие содержит кусок вывода или служебные сигналы (например, finish)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "SSE-стрим с чанками",
                content = [Content(
                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    schema = Schema(type = "string"),
                    examples = [ExampleObject(
                        name = "sseExample",
                        value = "event: message\ndata: {\"delta\":\"Прив\"}\n\n"
                    )]
                )]
            ),
            ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "429", description = "Лимит запросов превышен (Rate limit)"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервиса")
        ]
    )
    @PostMapping(
        "/completions:stream",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun stream(
        @RequestBody(
            required = true,
            description = "Запрос на генерацию ответа (стрим)",
            content = [Content(schema = Schema(implementation = ChatRequestDto::class))]
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody
        req: ChatRequestDto,
    ): Flow<ServerSentEvent<Any>> = chatService.stream(req)
}
