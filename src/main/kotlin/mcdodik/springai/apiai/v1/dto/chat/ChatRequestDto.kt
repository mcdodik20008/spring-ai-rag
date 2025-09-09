package mcdodik.springai.apiai.v1.dto.chat

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import mcdodik.springai.apiai.v1.dto.prompt.PromptShapingDto
import mcdodik.springai.apiai.v1.dto.reterieval.RetrievalDto

@Schema(description = "Запрос на генерацию ответа чата")
data class ChatRequestDto(
    @field:NotBlank
    @field:Schema(description = "Идентификатор базы знаний", example = "kb-legal-ru")
    val kbId: String,
    @field:Schema(description = "ID сессии диалога", example = "sess-123", nullable = true)
    val sessionId: String? = null,
    @field:Size(min = 1, max = 50)
    @field:Schema(
        description = "История сообщений",
        example = """[{"role":"user","content":"Суммируй договор в 5 пунктах"}]""",
    )
    val messages: List<ChatMessageDto>,
    @field:Schema(description = "Параметры поиска контекста", nullable = true)
    val retrieval: RetrievalDto? = RetrievalDto(),
    @field:Schema(description = "Параметры формирования промпта", nullable = true)
    val promptShaping: PromptShapingDto? = PromptShapingDto(),
    @field:NotBlank
    @field:Schema(description = "Имя модели", example = "gpt-4o-mini")
    val model: String,
    @field:DecimalMin("0.0") @field:DecimalMax("2.0")
    @field:Schema(description = "Температура", example = "0.2", defaultValue = "0.2", minimum = "0.0", maximum = "2.0")
    val temperature: Double = 0.2,
    @field:Positive @field:Max(32768)
    @field:Schema(description = "Лимит токенов вывода", example = "800", defaultValue = "800", maximum = "32768")
    val maxTokens: Int = 800,
)
