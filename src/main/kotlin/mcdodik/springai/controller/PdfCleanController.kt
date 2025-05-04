package mcdodik.springai.controller

import mcdodik.springai.utils.book.model.PdfCleanRequest
import mcdodik.springai.utils.book.parser.PdfCleaner
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/pdf")
class PdfCleanController(
    private val cleanerFactory: PdfCleaner
) {

    @PostMapping("/clean", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun cleanPdf(
        @RequestParam("file") file: MultipartFile,
        @RequestPart("params") params: PdfCleanRequest
    ): ResponseEntity<ByteArray> {
        val cleanedStream = cleanerFactory.cleanPdf(file.inputStream, params)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cleaned.pdf")
            .body(cleanedStream.readAllBytes())
    }
}
