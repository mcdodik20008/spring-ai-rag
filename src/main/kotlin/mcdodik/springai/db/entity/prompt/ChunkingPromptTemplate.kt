package mcdodik.springai.db.entity.prompt

import java.time.LocalDateTime
import java.util.UUID

data class ChunkingPromptTemplate(
    val id: UUID,
    val domainName: String,
    val userDescription: String,
    val generatedPrompt: String,
    val createdAt: LocalDateTime
)