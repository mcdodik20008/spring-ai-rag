package mcdodik.springai.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mcdodik.springai.api.service.DocumentInfoService
import mcdodik.springai.db.entity.rag.DocumentInfo
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Documents", description = "Чтение и удаление метаданных документов (Не тестил)")
@RestController
@RequestMapping("/api/documents", produces = [MediaType.APPLICATION_JSON_VALUE])
class DocumentInfoController(
    private val service: DocumentInfoService,
) {

    @Operation(
        summary = "Список всех документов",
        description = "Возвращает полный список метаданных документов."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список найден",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = ArraySchema(schema = Schema(implementation = DocumentInfo::class)),
                    examples = [ExampleObject(
                        name = "documentsExample",
                        value = EXAMPLE_BODY
                    )]
                )]
            )
        ]
    )
    @GetMapping
    fun getAll(): List<DocumentInfo> = service.getAll()

    @Operation(
        summary = "Получить документ по ID",
        description = "Возвращает метаданные одного документа по UUID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Документ найден",
                content = [Content(schema = Schema(implementation = DocumentInfo::class))]
            ),
            ApiResponse(responseCode = "404", description = "Документ не найден")
        ]
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(
            description = "UUID документа",
            required = true,
            example = "5b1f3e0b-9c5d-4a07-8f3a-0d7c9a2b9e11",
            schema = Schema(format = "uuid")
        )
        @PathVariable id: UUID,
    ): DocumentInfo = service.getById(id)

    @Operation(
        summary = "Найти документы по имени файла",
        description = "Фильтрует документы по точному имени файла (без пути)."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Список найден",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = ArraySchema(schema = Schema(implementation = DocumentInfo::class))
                )]
            )
        ]
    )
    @GetMapping("/by-file-name")
    fun getByFileName(
        @Parameter(
            description = "Имя файла без пути; регистр учитывается (если сервис так реализован)",
            example = "invoice_2024_07_15.pdf",
            schema = Schema(defaultValue = "sample.pdf")
        )
        @RequestParam fileName: String,
    ): List<DocumentInfo> = service.getByFileName(fileName)

    @Operation(
        summary = "Удалить документ по ID",
        description = "Удаляет документ по UUID. Возвращает 204 No Content при успехе."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Удалено"),
            ApiResponse(responseCode = "404", description = "Документ не найден")
        ]
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(
            description = "UUID документа для удаления",
            required = true,
            example = "5b1f3e0b-9c5d-4a07-8f3a-0d7c9a2b9e11",
            schema = Schema(format = "uuid")
        )
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    companion object {
        const val EXAMPLE_BODY = """[
  {
    "id":"5b1f3e0b-9c5d-4a07-8f3a-0d7c9a2b9e11",
    "fileName":"invoice_2024_07_15.pdf",
    "extension":"pdf",
    "hash":"a3f1c2ef...",
    "chunkCount":12,
    "createdAt":"2025-08-20T14:23:11",
    "summary":"Инвойс за июль 2024 г., сумма 124 500 ₽"
  }
]"""
    }
}

