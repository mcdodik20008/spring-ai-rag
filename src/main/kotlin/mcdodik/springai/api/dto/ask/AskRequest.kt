package mcdodik.springai.api.dto.ask

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Запрос на ответ от RAG-системы.",
    requiredProperties = ["question", "userId"],
)
data class AskRequest(
    @field:Schema(
        description = "Вопрос пользователя.",
        example = "Суммируй RFC 9114 (HTTP/3) в 5 пунктах",
    )
    val question: String,
    @field:Schema(
        description = "Уникальный идентификатор пользователя.",
        example = "123",
    )
    val userId: Long,
    @field:Schema(
        description = "Уникальный идентификатор диалога. Если не указан, будет создан новый диалог.",
        example = "456",
        nullable = true,
    )
    val conversationId: Long? = null,
)
