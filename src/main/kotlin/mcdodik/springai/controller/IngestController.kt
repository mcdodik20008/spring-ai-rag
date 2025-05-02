package mcdodik.springai.controller

import mcdodik.springai.service.RagService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/docs")
class IngestController(private val rag: RagService) {

    @PostMapping("/ingest")
    fun ingest(@RequestBody body: String) =
        rag.ingest(body)
}