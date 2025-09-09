package mcdodik.springai.apiai.v1.dto.chat

import io.swagger.v3.oas.annotations.media.Schema
import mcdodik.springai.apiai.v1.dto.UsageDto
import mcdodik.springai.apiai.v1.dto.reterieval.RetrievalDiagnosticsDto

@Schema(description = "Ответ чата")
data class ChatResponseDto(
    @field:Schema(description = "ID выполнения (run)", example = "2b4d7aa1-7b7b-4c77-9b84-0d8c2b2e3a11")
    val runId: String,
    @field:Schema(description = "ID сессии, если был передан", example = "sess-123", nullable = true)
    val sessionId: String?,
    @field:Schema(description = "Выходные сообщения/структуры модели")
    val output: ChatOutputDto,
    @field:Schema(description = "Диагностика RAG", nullable = true)
    val retrievalDiagnostics: RetrievalDiagnosticsDto? = null,
    @field:Schema(description = "Статистика токенов", nullable = true)
    val usage: UsageDto? = null,
    @field:Schema(description = "Суммарная задержка запроса, мс", example = "410", nullable = true)
    val latencyMs: Long? = null,
)
