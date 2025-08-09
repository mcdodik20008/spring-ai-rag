package mcdodik.springai.api.dto

import java.util.UUID

/** Response DTO for topic search. */
data class FindByTopicResponse(
    val items: List<Item>
) {
    data class Item(
        val id: UUID,
        val domainName: String,
        val topic: String?,
        val score: Double,           // косинусная схожесть (0..1)
        val preview: String          // первые 200 символов промпта
    )
}
