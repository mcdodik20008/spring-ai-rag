package mcdodik.springai.api.dto.ask

/**
 * DTO для ответа от LLM.
 * @param answer Сгенерированный ответ.
 * @param conversationId ID диалога, в котором произошел обмен сообщениями.
 */
data class AskResponse(
    val answer: String,
    val conversationId: Long,
)
