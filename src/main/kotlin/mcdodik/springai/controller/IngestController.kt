package mcdodik.springai.controller

import mcdodik.springai.service.RagService
import mcdodik.springai.utils.file.DelegatingMultipartFileFactory
import org.springframework.http.HttpHeaders
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
        @RequestBody body: String
    ): ResponseEntity<Any> {
        val file = factory.create(body)
        rag.ingest(file)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/ingest/pdf", consumes = ["multipart/form-data"])
    fun ingestPdf(
        @RequestPart("file") file: MultipartFile,
        @RequestHeader(HttpHeaders.CONTENT_TYPE) contentType: String
    ): ResponseEntity<Any> {
        rag.ingest(file)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}