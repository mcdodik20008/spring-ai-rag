package mcdodik.springai.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mcdodik.springai.api.dto.FindByTopicRequest
import mcdodik.springai.api.dto.FindByTopicResponse
import mcdodik.springai.api.service.PromptGenerationService
import mcdodik.springai.db.entity.prompt.ChunkingPromptTemplate
import mcdodik.springai.db.entity.prompt.PromptGenerationRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Prompt", description = "Генерация и поиск шаблонов промптов по доменам (Пока что не работает)")
@RestController
@RequestMapping("/api/prompt", produces = [MediaType.APPLICATION_JSON_VALUE])
class PromptGeneratorController(
    private val service: PromptGenerationService,
) {

    @Operation(
        summary = "Сгенерировать шаблон промпта по домену",
        description = "Принимает описание предметной области и генерирует Chunking Prompt Template."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Шаблон успешно сгенерирован",
                content = [Content(schema = Schema(implementation = ChunkingPromptTemplate::class))]
            ),
            ApiResponse(responseCode = "400", description = "Неверный запрос"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PostMapping("/generate", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun generatePrompt(
        @RequestBody(
            required = true,
            description = "Домен и пользовательское описание для генерации шаблона",
            content = [Content(
                schema = Schema(implementation = PromptGenerationRequest::class),
                examples = [ExampleObject(
                    name = "generateExample",
                    value = """
                    {
                      "domainName": "Oil&Gas Legal Docs",
                      "userDescription": "Разбивай документы на смысловые блоки: заголовок, реквизиты, обязательства, штрафы; выноси ключевые определения."
                    }
                    """
                )]
            )]
        )
        @org.springframework.web.bind.annotation.RequestBody
        request: PromptGenerationRequest,
    ): ChunkingPromptTemplate {
        return service.generatePrompt(request.domainName, request.userDescription)
    }

    @Operation(
        summary = "Найти шаблоны по теме",
        description = "Ищет сохранённые шаблоны по теме, возвращает top-k результатов с оценкой схожести."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Результаты найдены",
                content = [Content(schema = Schema(implementation = FindByTopicResponse::class))]
            ),
            ApiResponse(responseCode = "400", description = "Неверный запрос"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PostMapping("/find-by-topic", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun findByTopic(
        @RequestBody(
            required = true,
            description = "Запрос поиска по теме c top-k и порогом схожести",
            content = [Content(
                schema = Schema(implementation = FindByTopicRequest::class),
                examples = [ExampleObject(
                    name = "findByTopicExample",
                    value = """
                    {
                      "topic": "contract penalties",
                      "k": 5,
                      "minSim": 0.6
                    }
                    """
                )]
            )]
        )
        @org.springframework.web.bind.annotation.RequestBody
        req: FindByTopicRequest,
    ): FindByTopicResponse {
        val results = service.findByTopic(req.topic, req.k, req.minSim)
        return FindByTopicResponse(
            items = results.map {
                FindByTopicResponse.Item(
                    id = it.template.id,
                    domainName = it.template.domainName,
                    topic = it.template.topic,
                    score = it.score,
                    preview = it.template.generatedPrompt.take(SYMBOL_TO_VIEW),
                )
            },
        )
    }

    companion object {
        const val SYMBOL_TO_VIEW = 200
    }
}
