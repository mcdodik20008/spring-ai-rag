package mcdodik.springai.apiai.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.chat.ChatResponseDto
import mcdodik.springai.apiai.v1.serivces.RunsService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Tag(name = "Runs", description = "История запусков/ответов чата (Заглушка)")
//@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1", produces = [MediaType.APPLICATION_JSON_VALUE])
class RunsController(
    private val runsService: RunsService,
) {

    @Operation(
        summary = "Получить run по ID",
        description = "Возвращает сохранённый ответ чата и метаданные выполнения."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Найден",
                content = [Content(schema = Schema(implementation = ChatResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Не найдено"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @GetMapping("/runs/{runId}")
    fun get(
        @Parameter(description = "Идентификатор run’а", example = "run_01J2Z6Z7Z6W0R7W3E2KQ1VQ1V4")
        @PathVariable runId: String,
    ): Mono<ChatResponseDto> = runsService.get(runId)

    @Operation(
        summary = "Список run’ов",
        description = "Возвращает страницу run’ов с фильтрами по базе знаний и по времени (ISO-8601)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ок",
                content = [Content(
                    schema = Schema(implementation = PageDto::class),
                    examples = [ExampleObject(
                        name = "runsPageExample",
                        value = """
                        {
                          "items": [
                            {
                              "runId": "run_01J2Z6Z7...",
                              "sessionId": "sess-123",
                              "output": { "messages": [ { "role": "assistant", "content": "Привет!" } ] },
                              "retrievalDiagnostics": { "chunksUsed": 3 },
                              "usage": { "promptTokens": 120, "completionTokens": 85, "totalTokens": 205 },
                              "latencyMs": 410
                            }
                          ],
                          "total": 1,
                          "limit": 50,
                          "offset": 0
                        }
                        """
                    )]
                )]
            ),
            ApiResponse(responseCode = "400", description = "Неверные параметры"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "429", description = "Лимит запросов превышен"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @GetMapping("/runs")
    fun list(
        @Parameter(description = "ID базы знаний", example = "kb-legal-ru", schema = Schema(nullable = true))
        @RequestParam("kb_id", required = false) kbId: String?,

        @Parameter(
            description = "С какого времени (ISO-8601). Примеры: `2025-08-26T00:00:00Z`, `2025-08-26T12:34:56+03:00`",
            example = "2025-08-26T00:00:00Z",
            schema = Schema(nullable = true, format = "date-time")
        )
        @RequestParam("from", required = false) from: String?,

        @Parameter(
            description = "По какое время (ISO-8601) включительно/исключительно — по договорённости API",
            example = "2025-08-26T23:59:59Z",
            schema = Schema(nullable = true, format = "date-time")
        )
        @RequestParam("to", required = false) to: String?,

        @Parameter(description = "Размер страницы", example = "50", schema = Schema(defaultValue = "50", minimum = "1", maximum = "500"))
        @RequestParam("limit", defaultValue = "50") limit: Int,

        @Parameter(description = "Смещение", example = "0", schema = Schema(defaultValue = "0", minimum = "0"))
        @RequestParam("offset", defaultValue = "0") offset: Int,
    ): Mono<PageDto<ChatResponseDto>> = runsService.list(kbId, from, to, limit, offset)
}
