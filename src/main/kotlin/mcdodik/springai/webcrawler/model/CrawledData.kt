package mcdodik.springai.webcrawler.model

import java.time.LocalDateTime
import java.util.UUID

data class CrawledData(
    val id: UUID = UUID.randomUUID(),
    val source: String,
    val url: String,
    val title: String,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val metadata: Map<String, Any> = emptyMap()
)
