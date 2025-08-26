package mcdodik.springai.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mcdodik.springai.api.dto.EmptyParams
import mcdodik.springai.api.dto.PdfCleanRequest
import mcdodik.springai.infrastructure.youtube.model.YoutubeIngestRequest
import mcdodik.springai.infrastructure.youtube.service.api.YoutubeSubtitleService
import mcdodik.springai.rag.service.RagService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile


@Tag(
    name = "Ingest",
    description = "Загрузка и индексация документов в базу знаний (Markdown, PDF, YouTube)"
)
@RestController
@RequestMapping("/api/docs/ingest")
class IngestController(
    private val rag: RagService,
    private val youtube: YoutubeSubtitleService,
) {
    private val response =
        """
        Ваш файл успешно обработан и сохранён в базу знаний.
        Добавленная информация будет использоваться во время ответа на последующие вопросы.
        """.trimIndent()

    @Operation(
        summary = "Загрузить Markdown",
        description = "Принимает файл в формате Markdown и индексирует его в базе знаний."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Файл успешно проиндексирован",
                content = [Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = Schema(type = "string"),
                    examples = [ExampleObject(value = "Ваш файл успешно обработан и сохранён в базу знаний.")]
                )]
            ),
            ApiResponse(responseCode = "400", description = "Некорректный файл"),
            ApiResponse(responseCode = "500", description = "Ошибка обработки")
        ]
    )
    @PostMapping("/markdown", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun processMarkdown(
        @Parameter(
            description = "Markdown-файл (.md) для загрузки",
            required = true
        )
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<String> {
        rag.ingest(file, EmptyParams)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "Загрузить субтитры YouTube (У меня не работало)",
        description = "По videoId получает субтитры YouTube и индексирует их в базе знаний."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Субтитры успешно загружены",
                content = [Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = Schema(type = "string")
                )]
            ),
            ApiResponse(responseCode = "404", description = "Видео или субтитры не найдены"),
            ApiResponse(responseCode = "502", description = "Ошибка при обращении к YouTube")
        ]
    )
    @PostMapping("/youtube")
    suspend fun ingest(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Параметры загрузки субтитров",
            content = [Content(schema = Schema(implementation = YoutubeIngestRequest::class))]
        )
        @Valid @RequestBody req: YoutubeIngestRequest,
    ): ResponseEntity<String> {
        val file = youtube.getFile(videoId = req.normalizedVideoId())
        rag.ingest(file, EmptyParams)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "Загрузить PDF (Раньше работал.. Без AI)",
        description = "Принимает PDF-файл и индексирует его с опциями предобработки."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Файл успешно загружен",
                content = [Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = Schema(type = "string")
                )]
            ),
            ApiResponse(responseCode = "400", description = "Некорректные параметры"),
            ApiResponse(responseCode = "500", description = "Ошибка обработки PDF")
        ]
    )
    @PostMapping("/pdf", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun ingestPdf(
        @Parameter(description = "PDF-файл для загрузки", required = true)
        @RequestParam("file") file: MultipartFile,

        @Parameter(description = "Пропустить первые страницы", example = "1")
        @RequestParam("skipPages") skipPages: Int,

        @Parameter(description = "Отбросить страницы с конца", example = "2")
        @RequestParam("throwPagesFromEnd") throwPagesFromEnd: Int,

        @Parameter(description = "Количество строк в header/footer, которые надо выкинуть", example = "3")
        @RequestParam("headerFooterLines") headerFooterLines: Int,

        @Parameter(description = "Порог повторов для удаления дубликатов текста", example = "0.8")
        @RequestParam("repeatThreshold") repeatThreshold: Double,
    ): ResponseEntity<Any> {
        val params = PdfCleanRequest(skipPages, throwPagesFromEnd, headerFooterLines, repeatThreshold)
        rag.ingest(file, params)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
