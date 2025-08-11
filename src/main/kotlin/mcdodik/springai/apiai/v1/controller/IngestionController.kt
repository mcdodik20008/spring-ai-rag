package mcdodik.springai.apiai.v1.controller

import jakarta.validation.Valid
import mcdodik.springai.apiai.v1.dto.DocumentDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionRequestDto
import mcdodik.springai.apiai.v1.dto.ingestion.IngestionResponseDto
import mcdodik.springai.apiai.v1.serivces.IngestionService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1")
class IngestionController(
    private val ingestionService: IngestionService,
) {
    @PostMapping("/ingestions", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun startIngestion(
        @Valid @RequestBody req: IngestionRequestDto,
    ): Mono<IngestionResponseDto> = ingestionService.start(req)

    @GetMapping("/ingestions/{jobId}")
    fun getIngestion(
        @PathVariable jobId: String,
    ): Mono<IngestionResponseDto> = ingestionService.status(jobId)

    @PostMapping(
        "/documents",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun uploadDocument(
        @RequestPart("file") file: MultipartFile,
        @RequestParam("kb_id") kbId: String,
        @RequestParam(name = "llm_chunking", required = false, defaultValue = "true") llmChunking: Boolean,
        @RequestParam(name = "chunking_prompt_id", required = false) chunkingPromptId: String?,
    ): Mono<DocumentDto> = ingestionService.upload(kbId, file, llmChunking, chunkingPromptId)
}
