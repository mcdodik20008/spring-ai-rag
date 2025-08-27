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
import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionRequestDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionResponseDto
import mcdodik.springai.apiai.v1.serivces.IngestionService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono

@Tag(name = "Ingestion", description = "Запуск ingestion-джоб и загрузка документов (Заглушка)")
@RestController
@RequestMapping("/v1", produces = [MediaType.APPLICATION_JSON_VALUE])
class IngestionController(
    private val ingestionService: IngestionService,
) {

    @Operation(
        summary = "Старт ingestion-джобы",
        description = "Создаёт ingestion-задачу и возвращает её идентификатор и текущий статус."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Джоба создана",
                content = [Content(schema = Schema(implementation = IngestionResponseDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "429", description = "Лимит запросов превышен"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PostMapping("/ingestions", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun startIngestion(
        @RequestBody(
            required = true,
            description = "Параметры старта ingestion-джобы",
            content = [Content(
                schema = Schema(implementation = IngestionRequestDto::class),
                examples = [ExampleObject(
                    name = "startExample",
                    value = """
                    {
                      "kbId": "kb-legal-ru",
                      "source": {"type":"youtube","videoId":"dQw4w9WgXcQ"},
                      "llmChunking": true,
                      "chunkingPromptId": "7a0f7f3a-23a1-4b2b-9c8f-3f1e8ddf0b77"
                    }
                    """
                )]
            )]
        )
        @Valid
        @org.springframework.web.bind.annotation.RequestBody
        req: IngestionRequestDto,
    ): Mono<IngestionResponseDto> = ingestionService.start(req)

    @Operation(
        summary = "Статус ingestion-джобы",
        description = "Возвращает текущий статус задачи по её идентификатору."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Статус получен",
                content = [Content(schema = Schema(implementation = IngestionResponseDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Джоба не найдена"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @GetMapping("/ingestions/{jobId}")
    fun getIngestion(
        @Schema(description = "ID джобы", example = "job_01J2Z6Z7Z6W0R7W3E2KQ1VQ1V4")
        @PathVariable jobId: String,
    ): Mono<IngestionResponseDto> = ingestionService.status(jobId)

    @Operation(
        summary = "Загрузить документ (multipart)",
        description = "Загружает файл в базу знаний. При `llm_chunking=true` текст будет автоматически разбит на чанки по LLM-промпту."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Файл принят",
                content = [Content(schema = Schema(implementation = DocumentDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Некорректная форма или параметры"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "413", description = "Файл слишком большой"),
            ApiResponse(responseCode = "415", description = "Неподдерживаемый тип"),
            ApiResponse(responseCode = "429", description = "Лимит запросов превышен"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PostMapping(
        "/documents",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun uploadDocument(
        @RequestBody(
            required = true,
            description = "Multipart-форма: файл и параметры",
            content = [Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                examples = [ExampleObject(
                    name = "multipartExample",
                    summary = "Пример формы",
                    value = "{file: (binary), kb_id: kb-legal-ru, llm_chunking: true, chunking_prompt_id: 7a0f7f3a-23a1-4b2b-9c8f-3f1e8ddf0b77}"
                )]
            )]
        )
        @RequestPart("file") file: MultipartFile,

        @Schema(description = "ID базы знаний", example = "kb-legal-ru")
        @RequestParam("kb_id") kbId: String,

        @Schema(description = "Включить LLM-чанкование", defaultValue = "true", example = "true")
        @RequestParam(name = "llm_chunking", required = false, defaultValue = "true") llmChunking: Boolean,

        @Schema(description = "ID шаблона промпта для чанкования", nullable = true, example = "7a0f7f3a-23a1-4b2b-9c8f-3f1e8ddf0b77")
        @RequestParam(name = "chunking_prompt_id", required = false) chunkingPromptId: String?,
    ): Mono<DocumentDto> = ingestionService.upload(kbId, file, llmChunking, chunkingPromptId)
}
