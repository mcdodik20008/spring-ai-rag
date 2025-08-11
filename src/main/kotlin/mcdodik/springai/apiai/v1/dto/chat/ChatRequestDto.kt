package mcdodik.springai.apiai.v1.dto.chat

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import mcdodik.springai.apiai.v1.dto.prompt.PromptShapingDto
import mcdodik.springai.apiai.v1.dto.reterieval.RetrievalDto

data class ChatRequestDto(
    @field:NotBlank val kbId: String,
    val sessionId: String? = null,
    @field:Size(min = 1, max = 50) val messages: List<ChatMessageDto>,
    val retrieval: RetrievalDto? = RetrievalDto(),
    val promptShaping: PromptShapingDto? = PromptShapingDto(),
    @field:NotBlank val model: String,
    @field:DecimalMin("0.0") @field:DecimalMax("2.0") val temperature: Double = 0.2,
    @field:Positive @field:Max(32768) val maxTokens: Int = 800,
)
