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
import mcdodik.springai.apiai.v1.dto.ChunksUpsertRequestDto
import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.chat.ChunkDto
import mcdodik.springai.apiai.v1.serivces.DocumentsService
import mcdodik.springai.openapi.dto.PageChunkDto
import mcdodik.springai.openapi.dto.PageDocumentDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@Tag(name = "Documents", description = "Документы, чанки и управление контентом (Заглушка)")
@RestController
@RequestMapping("/v1", produces = [MediaType.APPLICATION_JSON_VALUE])
class DocumentsController(
    private val documentsService: DocumentsService,
) {

    @Operation(
        summary = "Получить документ по ID",
        description = "Возвращает метаданные одного документа."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Документ найден",
                content = [Content(schema = Schema(implementation = DocumentDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Документ не найден"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @GetMapping("/documents/{docId}")
    fun getDocument(
        @Parameter(description = "Идентификатор документа", example = "doc_01HZX6Z7Z6W0R7W3E2KQ1VQ1V4")
        @PathVariable docId: String,
    ): Mono<DocumentDto> = documentsService.get(docId)

    @Operation(
        summary = "Список документов",
        description = "Возвращает страницу документов по базе знаний с фильтрами."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Ок",
                content = [Content(schema = Schema(implementation = PageDocumentDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Неверные параметры"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "429", description = "Лимит запросов превышен"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @GetMapping("/documents")
    fun listDocuments(
        @Parameter(description = "ID базы знаний", example = "kb-legal-ru")
        @RequestParam("kb_id") kbId: String,

        @Parameter(description = "Поисковый запрос (full-text)", example = "договор поставки")
        @RequestParam("q", required = false) q: String?,

        @Parameter(description = "Статус документа", example = "READY", schema = Schema(allowableValues = ["PENDING", "READY", "FAILED"], defaultValue = "READY"))
        @RequestParam("status", required = false) status: String?,

        @Parameter(description = "Тег фильтрации", example = "contract")
        @RequestParam("tag", required = false) tag: String?,

        @Parameter(description = "Размер страницы", example = "50", schema = Schema(defaultValue = "50", minimum = "1", maximum = "500"))
        @RequestParam("limit", defaultValue = "50") limit: Int,

        @Parameter(description = "Смещение", example = "0", schema = Schema(defaultValue = "0", minimum = "0"))
        @RequestParam("offset", defaultValue = "0") offset: Int,
    ): Mono<PageDto<DocumentDto>> = documentsService.list(kbId, q, status, tag, limit, offset)

    @Operation(
        summary = "Удалить документ",
        description = "Удаляет документ по ID. Возвращает 204 при успехе."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Удалено"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Документ не найден"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @DeleteMapping("/documents/{docId}")
    fun deleteDocument(
        @Parameter(description = "Идентификатор документа", example = "doc_01HZX6Z7Z6W0R7W3E2KQ1VQ1V4")
        @PathVariable docId: String,
    ): Mono<Void> = documentsService.delete(docId)

    @Operation(
        summary = "Список чанков документа",
        description = "Возвращает страницу чанков по документу с опциональным поиском."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Ок",
                content = [Content(schema = Schema(implementation = PageChunkDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Неверные параметры"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Документ не найден"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @GetMapping("/documents/{docId}/chunks")
    fun listChunks(
        @Parameter(description = "Идентификатор документа", example = "doc_01HZX6Z7Z6W0R7W3E2KQ1VQ1V4")
        @PathVariable docId: String,

        @Parameter(description = "Размер страницы", example = "50", schema = Schema(defaultValue = "50", minimum = "1", maximum = "1000"))
        @RequestParam("limit", defaultValue = "50") limit: Int,

        @Parameter(description = "Смещение", example = "0", schema = Schema(defaultValue = "0", minimum = "0"))
        @RequestParam("offset", defaultValue = "0") offset: Int,

        @Parameter(description = "Поиск по тексту чанка", example = "неустойка")
        @RequestParam("search", required = false) search: String?,
    ): Mono<PageDto<ChunkDto>> = documentsService.listChunks(docId, limit, offset, search)

    @Operation(
        summary = "Upsert чанков",
        description = "Создаёт или обновляет чанки документа. При `embedIfMissing=true` отсутствующие эмбеддинги будут рассчитаны автоматически."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Обработано (без тела)"),
            // если оставляешь 200 с телом — поменяй описание/тип
            ApiResponse(responseCode = "400", description = "Неверное тело запроса"),
            ApiResponse(responseCode = "401", description = "Неавторизовано"),
            ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            ApiResponse(responseCode = "404", description = "Документ не найден"),
            ApiResponse(responseCode = "429", description = "Лимит запросов превышен"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка")
        ]
    )
    @PostMapping("/chunks:upsert", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun upsertChunks(
        @RequestBody(
            required = true,
            description = "Чанки для upsert",
            content = [Content(
                schema = Schema(implementation = ChunksUpsertRequestDto::class),
                examples = [ExampleObject(
                    name = "upsertExample",
                    value = """
                    {
                      "docId": "doc_01HZX6Z7Z6W0R7W3E2KQ1VQ1V4",
                      "chunks": [
                        { "id": "c1", "text": "Раздел 1. Общие положения", "tag": "intro" },
                        { "id": "c2", "text": "Ответственность сторон ...", "tag": "penalties" }
                      ],
                      "embedIfMissing": true
                    }
                    """
                )]
            )]
        )
        @Valid @org.springframework.web.bind.annotation.RequestBody
        req: ChunksUpsertRequestDto,
    ): Mono<Void> = documentsService.upsertChunks(req)
}
