package mcdodik.springai.apiai.v1.controller

import mcdodik.springai.apiai.v1.dto.ChunksUpsertRequestDto
import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.chat.ChunkDto
import mcdodik.springai.apiai.v1.serivces.DocumentsService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1")
class DocumentsController(
    private val documentsService: DocumentsService,
) {
    @GetMapping("/documents/{docId}")
    fun getDocument(
        @PathVariable docId: String,
    ): Mono<DocumentDto> = documentsService.get(docId)

    @GetMapping("/documents")
    fun listDocuments(
        @RequestParam("kb_id") kbId: String,
        @RequestParam("q", required = false) q: String?,
        @RequestParam("status", required = false) status: String?,
        @RequestParam("tag", required = false) tag: String?,
        @RequestParam("limit", defaultValue = "50") limit: Int,
        @RequestParam("offset", defaultValue = "0") offset: Int,
    ): Mono<PageDto<DocumentDto>> = documentsService.list(kbId, q, status, tag, limit, offset)

    @DeleteMapping("/documents/{docId}")
    fun deleteDocument(
        @PathVariable docId: String,
    ): Mono<Void> = documentsService.delete(docId)

    @GetMapping("/documents/{docId}/chunks")
    fun listChunks(
        @PathVariable docId: String,
        @RequestParam("limit", defaultValue = "50") limit: Int,
        @RequestParam("offset", defaultValue = "0") offset: Int,
        @RequestParam("search", required = false) search: String?,
    ): Mono<PageDto<ChunkDto>> = documentsService.listChunks(docId, limit, offset, search)

    @PostMapping("/chunks:upsert")
    fun upsertChunks(
        @RequestBody req: ChunksUpsertRequestDto,
    ): Mono<Void> = documentsService.upsertChunks(req)
}
