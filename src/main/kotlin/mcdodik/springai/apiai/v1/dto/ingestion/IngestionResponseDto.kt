package mcdodik.springai.apiai.v1.dto.ingestion

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Ответ на операции ingestion")
data class IngestionResponseDto(
    @field:Schema(description = "ID джобы", example = "job_01J2Z6Z7Z6W0R7W3E2KQ1VQ1V4")
    val jobId: String,
    @field:Schema(description = "Статус джобы", example = "PENDING", allowableValues = ["PENDING","RUNNING","READY","FAILED"])
    val status: String,
)
