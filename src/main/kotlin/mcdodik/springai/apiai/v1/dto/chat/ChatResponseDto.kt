package mcdodik.springai.apiai.v1.dto.chat

import mcdodik.springai.apiai.v1.dto.UsageDto
import mcdodik.springai.apiai.v1.dto.reterieval.RetrievalDiagnosticsDto

data class ChatResponseDto(
    val runId: String,
    val sessionId: String?,
    val output: ChatOutputDto,
    val retrievalDiagnostics: RetrievalDiagnosticsDto? = null,
    val usage: UsageDto? = null,
    val latencyMs: Long? = null,
)
