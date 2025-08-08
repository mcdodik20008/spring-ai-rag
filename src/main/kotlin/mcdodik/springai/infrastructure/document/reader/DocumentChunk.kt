package mcdodik.springai.infrastructure.document.reader

data class DocumentChunk(
    val content: String,
    val metadata: Map<String, Any?> = emptyMap()
)