package mcdodik.springai.api.controller

import mcdodik.springai.api.controller.model.PdfCleanRequest
import mcdodik.springai.utils.cleaner.DocumentCleaner
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/pdf")
class PdfCleanController(
    private val cleaner: DocumentCleaner
) {

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
