package mcdodik.springai.api.dto.ask

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Запрос на ответ от RAG", requiredProperties = ["question"])
data class AskRequest(
    @field:Schema(
        description = "Вопрос пользователя (ru).",
        example = "Суммируй RFC 9114 (HTTP/3) в 5 пунктах",
        defaultValue = "Кто такой Глен Гульд?",
    )
    val question: String,
)
