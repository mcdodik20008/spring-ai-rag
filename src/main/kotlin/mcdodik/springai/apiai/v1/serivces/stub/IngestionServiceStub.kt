package mcdodik.springai.apiai.v1.serivces.stub

import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionRequestDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionResponseDto
import mcdodik.springai.apiai.v1.serivces.IngestionService
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

// @Profile("stub")
@Service
@Primary
class IngestionServiceStub : IngestionService {
    override fun start(req: IngestionRequestDto): Mono<IngestionResponseDto> = Mono.just(IngestionResponseDto(jobId = "ing_${UUID.randomUUID()}", status = "queued"))

    override fun status(jobId: String): Mono<IngestionResponseDto> = Mono.just(IngestionResponseDto(jobId, status = "completed"))

    override fun upload(
        kbId: String,
        file: org.springframework.web.multipart.MultipartFile,
        llmChunking: Boolean,
        chunkingPromptId: String?,
    ): Mono<DocumentDto> =
        Mono.just(
            DocumentDto(
                id = "doc_${UUID.randomUUID()}",
                kbId = kbId,
                fileName = file.originalFilename ?: "file",
                mime = file.contentType,
                size = file.size,
                hash = "stub",
                status = "processing",
                createdAt = Instant.now().toString(),
                summary = null,
                tags = emptyList(),
            ),
        )
}
