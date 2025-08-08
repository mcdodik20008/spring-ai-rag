package mcdodik.springai.api.controller.rag

import mcdodik.springai.api.controller.responses.PdfCleanRequest
import mcdodik.springai.utils.cleaner.DocumentCleaner
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller for handling PDF cleaning operations.
 * Provides an endpoint to clean and process uploaded PDF files using custom parameters.
 */
@RestController
@RequestMapping("/api/pdf")
class PdfCleanController(
    /**
     * Service used to perform cleaning and processing of PDF documents.
     */
    private val cleaner: DocumentCleaner
) {

    /**
     * Handles POST requests to the "/api/pdf/clean" endpoint.
     * Accepts a PDF file along with additional parameters for cleaning and processing.
     * Returns a cleaned PDF file as a downloadable attachment.
     *
     * @param file The uploaded PDF file.
     * @param skipPages Number of pages to skip from the beginning of the document.
     * @param throwPagesFromEnd Number of pages to exclude from the end of the document.
     * @param headerFooterLines Number of lines to consider as headers or footers and remove them.
     * @param repeatThreshold Threshold value to detect and eliminate repeated content.
     * @return A [ResponseEntity] containing the cleaned PDF file as a byte array,
     *         with appropriate HTTP headers for download.
     */
    @PostMapping("/clean", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun cleanPdf(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("skipPages") skipPages: Int,
        @RequestParam("throwPagesFromEnd") throwPagesFromEnd: Int,
        @RequestParam("headerFooterLines") headerFooterLines: Int,
        @RequestParam("repeatThreshold") repeatThreshold: Double
    ): ResponseEntity<ByteArray> {
        val params = PdfCleanRequest(skipPages, throwPagesFromEnd, headerFooterLines, repeatThreshold)
        val cleanedStream = cleaner.doIt(file.inputStream, params)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cleaned.pdf")
            .body(cleanedStream.readAllBytes())
    }
}
