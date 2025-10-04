package mcdodik.springai.api.restcontroller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mcdodik.springai.api.dto.ingest.CustomMultipartFile
import mcdodik.springai.api.dto.ingest.EmptyParams
import mcdodik.springai.api.dto.ingest.PdfCleanRequest
import mcdodik.springai.infrastructure.youtube.model.YoutubeIngestRequest
import mcdodik.springai.infrastructure.youtube.service.api.YoutubeSubtitleService
import mcdodik.springai.rag.service.api.RagService
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Tag(
    name = "Ingest",
    description = "Загрузка и индексация документов в базу знаний (Markdown, PDF, YouTube)",
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
        description = "Принимает файл в формате Markdown и индексирует его в базе знаний.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Файл успешно проиндексирован",
                content = [
                    Content(
                        mediaType = MediaType.TEXT_PLAIN_VALUE,
                        schema = Schema(type = "string"),
                        examples = [ExampleObject(value = "Ваш файл успешно обработан и сохранён в базу знаний.")],
                    ),
                ],
            ),
            ApiResponse(responseCode = "400", description = "Некорректный файл"),
            ApiResponse(responseCode = "500", description = "Ошибка обработки"),
        ],
    )
    @PostMapping(
        "/markdown",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    fun processMarkdown(
        @RequestPart("file") file: FilePart,
    ): Mono<ResponseEntity<String>> =
        DataBufferUtils
            .join(file.content())
            .map { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)
                CustomMultipartFile(
                    name = "file",
                    originalFilename = file.filename(),
                    contentType = guessContentType(file.filename()),
                    content = bytes,
                )
            }.flatMap { mf ->
                Mono
                    .fromCallable {
                        rag.ingest(mf, EmptyParams)
                        ResponseEntity
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(response)
                    }.subscribeOn(Schedulers.boundedElastic())
            }

    @Operation(
        summary = "Загрузить субтитры YouTube (У меня не работало)",
        description = "По videoId получает субтитры YouTube и индексирует их в базе знаний.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Субтитры успешно загружены",
                content = [
                    Content(
                        mediaType = MediaType.TEXT_PLAIN_VALUE,
                        schema = Schema(type = "string"),
                    ),
                ],
            ),
            ApiResponse(responseCode = "404", description = "Видео или субтитры не найдены"),
            ApiResponse(responseCode = "502", description = "Ошибка при обращении к YouTube"),
        ],
    )
    @PostMapping("/youtube")
    suspend fun ingest(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Параметры загрузки субтитров",
            content = [Content(schema = Schema(implementation = YoutubeIngestRequest::class))],
        )
        @Valid
        @RequestBody req: YoutubeIngestRequest,
    ): ResponseEntity<String> {
        val file = youtube.getFile(videoId = req.normalizedVideoId())
        rag.ingest(file, EmptyParams)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "Загрузить PDF (Раньше работал.. Без AI)",
        description = "Принимает PDF-файл и индексирует его с опциями предобработки.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Файл успешно загружен",
                content = [
                    Content(
                        mediaType = MediaType.TEXT_PLAIN_VALUE,
                        schema = Schema(type = "string"),
                    ),
                ],
            ),
            ApiResponse(responseCode = "400", description = "Некорректные параметры"),
            ApiResponse(responseCode = "500", description = "Ошибка обработки PDF"),
        ],
    )
    @PostMapping(
        "/pdf",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    fun ingestPdf(
        @Parameter(description = "PDF-файл для загрузки", required = true)
        @RequestPart("file") file: FilePart,
    ): Mono<ResponseEntity<String>> {
        val params = EmptyParams

        return DataBufferUtils
            .join(file.content())
            .map { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                DataBufferUtils.release(dataBuffer)
                CustomMultipartFile(
                    name = "file",
                    originalFilename = file.filename(),
                    contentType = "application/pdf",
                    content = bytes,
                )
            }.flatMap { mf ->
                Mono
                    .fromCallable {
                        rag.ingest(mf, params)
                        ResponseEntity
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(response)
                    }.subscribeOn(Schedulers.boundedElastic())
            }
    }

    private fun guessContentType(filename: String): String? =
        when {
            filename.endsWith(".md", true) -> "text/markdown"
            filename.endsWith(".txt", true) -> "text/plain"
            else -> null
        }
}
