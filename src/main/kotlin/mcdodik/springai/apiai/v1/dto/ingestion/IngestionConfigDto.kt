package mcdodik.springai.apiai.v1.dto.ingestion

import jakarta.validation.constraints.Min
import mcdodik.springai.apiai.v1.dto.OcrDto

data class IngestionConfigDto(
    val chunkingPromptId: String? = null,
    val llmChunking: Boolean = true,
    @field:Min(0) val chunkSize: Int = 0,
    @field:Min(0) val overlap: Int = 0,
    val language: String? = "auto",
    val extractMetadata: Boolean = true,
    val mimeHints: List<String>? = null,
    val ocr: OcrDto? = null,
)
