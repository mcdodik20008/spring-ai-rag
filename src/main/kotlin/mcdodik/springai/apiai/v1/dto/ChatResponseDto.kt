package mcdodik.springai.apiai.v1.dto

data class ChatResponseDto(
    val runId: String,
    val sessionId: String?,
    val output: ChatOutputDto,
    val retrievalDiagnostics: RetrievalDiagnosticsDto? = null,
    val usage: UsageDto? = null,
    val latencyMs: Long? = null,
)
