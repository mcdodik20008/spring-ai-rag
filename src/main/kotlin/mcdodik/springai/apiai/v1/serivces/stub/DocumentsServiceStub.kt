package mcdodik.springai.apiai.v1.serivces.stub

import mcdodik.springai.apiai.v1.dto.ChunksUpsertRequestDto
import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.chat.ChunkDto
import mcdodik.springai.apiai.v1.serivces.DocumentsService
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

@Primary
@Service
//@Profile("stub")
class DocumentsServiceStub : DocumentsService {
    override fun get(docId: String): Mono<DocumentDto> =
        Mono.just(
            DocumentDto(
                id = docId, kbId = "kb_1", fileName = "stub.md", mime = "text/markdown",
                size = 123, hash = "stub", status = "ready",
                createdAt = Instant.now().toString(), summary = "stub", tags = listOf("demo"),
            ),
        )

    override fun list(
        kbId: String,
        q: String?,
        status: String?,
        tag: String?,
        limit: Int,
        offset: Int,
    ): Mono<PageDto<DocumentDto>> =
        Mono.just(
            PageDto(
                items =
                    listOf(
                        DocumentDto(
                            "doc_1", kbId, "stub.md", "text/markdown", 123, "h", "ready",
                            Instant.now().toString(), "sum", listOf("demo"),
                        ),
                    ),
                total = 1,
                limit = limit,
                offset = offset,
            ),
        )

    override fun delete(docId: String): Mono<Void> = Mono.empty()

    override fun listChunks(
        docId: String,
        limit: Int,
        offset: Int,
        search: String?,
    ): Mono<PageDto<ChunkDto>> =
        Mono.just(
            PageDto(
                items =
                    listOf(
                        ChunkDto("ch_1", docId, 0, "stub content", 20, mapOf("section" to "Intro"), score = 0.9),
                    ),
                total = 1,
                limit = limit,
                offset = offset,
            ),
        )

    override fun upsertChunks(req: ChunksUpsertRequestDto): Mono<Void> = Mono.empty()
}
