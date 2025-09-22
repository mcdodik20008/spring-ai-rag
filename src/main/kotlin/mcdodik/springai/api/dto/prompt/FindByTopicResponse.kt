package mcdodik.springai.api.dto.prompt

import java.util.UUID

/** Response DTO for topic search. */
data class FindByTopicResponse(
    val items: List<Item>,
) {
    data class Item(
        val id: UUID,
        val domainName: String,
        val topic: String?,
        val score: Double,
        val preview: String,
    )
}
