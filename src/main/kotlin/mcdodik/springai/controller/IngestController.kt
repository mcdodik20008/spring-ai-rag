package mcdodik.springai.controller

import mcdodik.springai.controller.model.EmptyParams
import mcdodik.springai.controller.model.PdfCleanRequest
import mcdodik.springai.rag.services.RagService
import mcdodik.springai.utils.mulripartcreator.DelegatingMultipartFileFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/docs")
class IngestController(
    private val rag: RagService
) {

    val response =
        "Ваш файл успешно обработан и сохранён в базу знаний. \nДобавленная информация будет использоваться во время ответа на последующие вопросы."

    @PostMapping("/ingest/markdown", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun processMarkdown(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        rag.ingest(file, EmptyParams)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/ingest/pdf", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
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