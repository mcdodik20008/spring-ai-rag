package mcdodik.springai.apiai.v1.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.prompt.PromptDto
import mcdodik.springai.apiai.v1.serivces.PromptsService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Tag(name = "Prompts", description = "CRUD для шаблонов промптов (Заглушка)")
//@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/prompts", produces = [MediaType.APPLICATION_JSON_VALUE])
class PromptsController(
    private val promptsService: PromptsService,
) {

    @Operation(
        summary = "Список промптов",
        description = "Возвращает страницу промптов с фильтрацией по типу и пагинацией."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Ок",
                content = [Content(
                    schema = Schema(implementation = PageDto::class),
                    examples = [ExampleObject(
                        name = "pageExample",
                        value = """
                        {
                          "items": [
                            {
                              "id": "prt_01HZ...7W3",
                              "name": "Chunking RU v1",
                              "type": "CHUNKING",
                              "template": "Ты — интеллектуальный редактор...",
                              "variables": ["domainName","rules"],
                              "version": 1,
                              "createdAt": "2025-08-26T07:41:00Z"
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
    @GetMapping
    fun list(
        @Parameter(
            description = "Фильтр по типу промпта (значения — как в enum PromptType)",
            example = "CHUNKING",
            schema = Schema(nullable = true)
        )
        @RequestParam("type", required = false) type: String?,

        @Parameter(description = "Размер страницы", example = "50", schema = Schema(defaultValue = "50", minimum = "1", maximum = "500"))
        @RequestParam("limit", defaultValue = "50") limit: Int,

        @Parameter(description = "Смещение", example = "0", schema = Schema(defaultValue = "0", minimum = "0"))
        @RequestParam("offset", defaultValue = "0") offset: Int,
    ): Mono<PageDto<PromptDto>> = promptsService.list(type, limit, offset)

    @Operation(
        summary = "Создать промпт",
        description = "Создаёт новый шаблон промпта."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Создано",
                content = [Content(schema = Schema(implementation = PromptDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Неверное тело запроса"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "409", description = "Конфликт (например, имя уже занято)"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(
        @RequestBody(
            required = true,
            description = "Данные нового промпта",
            content = [Content(
                schema = Schema(implementation = PromptDto::class),
                examples = [ExampleObject(
                    name = "createExample",
                    value = """
                    {
                      "name": "Chunking RU v1",
                      "type": "CHUNKING",
                      "template": "Ты — интеллектуальный редактор... (текст промпта)",
                      "variables": ["domainName","rules"]
                    }
                    """
                )]
            )]
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody
        prompt: PromptDto,
    ): Mono<PromptDto> = promptsService.create(prompt)

    @Operation(
        summary = "Получить промпт по ID",
        description = "Возвращает промпт по его идентификатору."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Найден",
                content = [Content(schema = Schema(implementation = PromptDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Не найден"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "ID промпта", example = "prt_01HZ...7W3")
        @PathVariable id: String,
    ): Mono<PromptDto> = promptsService.get(id)

    @Operation(
        summary = "Обновить промпт",
        description = "Полностью обновляет шаблон промпта по ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Обновлено",
                content = [Content(schema = Schema(implementation = PromptDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Неверное тело запроса"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Не найден"),
            ApiResponse(responseCode = "409", description = "Конфликт версий/имени"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun update(
        @Parameter(description = "ID промпта", example = "prt_01HZ...7W3")
        @PathVariable id: String,
        @RequestBody(
            required = true,
            description = "Данные промпта для обновления",
            content = [Content(schema = Schema(implementation = PromptDto::class))]
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody
        prompt: PromptDto,
    ): Mono<PromptDto> = promptsService.update(id, prompt)

    @Operation(
        summary = "Удалить промпт",
        description = "Удаляет промпт по ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Удалено"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Не найден"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "ID промпта", example = "prt_01HZ...7W3")
        @PathVariable id: String,
    ): Mono<Void> = promptsService.delete(id)
}
