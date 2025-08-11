package mcdodik.springai.apiai.v1.serivces

import mcdodik.springai.apiai.v1.dto.ChunksUpsertRequestDto
import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.chat.ChunkDto
import reactor.core.publisher.Mono

interface DocumentsService {
    fun get(docId: String): Mono<DocumentDto>

    fun list(
        kbId: String,
        q: String?,
        status: String?,
        tag: String?,
        limit: Int,
        offset: Int,
    ): Mono<PageDto<DocumentDto>>

    fun delete(docId: String): Mono<Void>

    fun listChunks(
        docId: String,
        limit: Int,
        offset: Int,
        search: String?,
    ): Mono<PageDto<ChunkDto>>

    fun upsertChunks(req: ChunksUpsertRequestDto): Mono<Void>
}
