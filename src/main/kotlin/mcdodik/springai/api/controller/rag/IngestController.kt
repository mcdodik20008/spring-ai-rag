package mcdodik.springai.api.controller.rag

import mcdodik.springai.api.dto.EmptyParams
import mcdodik.springai.api.dto.PdfCleanRequest
import mcdodik.springai.rag.service.RagService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller for handling document ingestion operations.
 * Provides endpoints to upload and process Markdown and PDF files.
 */
@RestController
@RequestMapping("/api/docs")
class IngestController(
    /**
     * Service used to process and store document data in the knowledge base.
     */
    private val rag: RagService
) {

    /**
     * Standard response message returned after successful file processing.
     * Indicates that the file was successfully processed and saved to the knowledge base.
     */
    val response =
        "Ваш файл успешно обработан и сохранён в базу знаний. \nДобавленная информация будет использоваться во время ответа на последующие вопросы."

    /**
     * Handles POST requests to the "/api/docs/ingest/markdown" endpoint.
     * Accepts a Markdown file as a multipart form data request.
     * Processes and ingests the file into the knowledge base.
     *
     * @param file The uploaded Markdown file.
     * @return A [ResponseEntity] with HTTP status 201 (CREATED) and the success message.
     */
    @PostMapping("/ingest/markdown", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun processMarkdown(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        rag.ingest(file, EmptyParams)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Handles POST requests to the "/api/docs/ingest/pdf" endpoint.
     * Accepts a PDF file along with additional parameters for cleaning and processing.
     * Processes and ingests the file into the knowledge base using specified parameters.
     *
     * @param file The uploaded PDF file.
     * @param skipPages Number of pages to skip from the beginning of the document.
     * @param throwPagesFromEnd Number of pages to exclude from the end of the document.
     * @param headerFooterLines Number of lines to consider as headers or footers and remove them.
     * @param repeatThreshold Threshold value to detect and eliminate repeated content.
     * @return A [ResponseEntity] with HTTP status 201 (CREATED) and the success message.
     */
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
