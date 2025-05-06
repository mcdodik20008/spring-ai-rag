package mcdodik.springai.controller

import mcdodik.springai.service.RagService
import mcdodik.springai.utils.cleaner.PdfCleanRequest
import mcdodik.springai.utils.mulripartcreator.DelegatingMultipartFileFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/docs")
class IngestController(
    private val rag: RagService,
    private val factory: DelegatingMultipartFileFactory
) {

    val response =
        "Ваш файл успешно обработан и сохранён в базу знаний. \nДобавленная информация будет использоваться во время ответа на последующие вопросы."

    @PostMapping("/ingest")
    fun ingest(
        @RequestBody body: String,
        @RequestPart() params: PdfCleanRequest,
    ): ResponseEntity<Any> {
        val file = factory.create(body)
        rag.ingest(file, params)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/ingest/pdf", consumes = ["multipart/form-data"])
    fun ingestPdf(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("skipPages") skipPages: Int,
        @RequestParam("throwPagesFromEnd") throwPagesFromEnd: Int,
        @RequestParam("headerFooterLines") headerFooterLines: Int,
        @RequestParam("repeatThreshold") repeatThreshold: Double
    ): ResponseEntity<Any> {
        val params = PdfCleanRequest(skipPages, throwPagesFromEnd, headerFooterLines, repeatThreshold)
        rag.ingest(file, params)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}