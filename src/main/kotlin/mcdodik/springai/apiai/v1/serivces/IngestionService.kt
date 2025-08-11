package mcdodik.springai.apiai.v1.serivces

import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionRequestDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionResponseDto
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono

interface IngestionService {
    fun start(req: IngestionRequestDto): Mono<IngestionResponseDto>

    fun status(jobId: String): Mono<IngestionResponseDto>

    fun upload(
        kbId: String,
        file: MultipartFile,
        llmChunking: Boolean,
        chunkingPromptId: String?,
    ): Mono<DocumentDto>
}
