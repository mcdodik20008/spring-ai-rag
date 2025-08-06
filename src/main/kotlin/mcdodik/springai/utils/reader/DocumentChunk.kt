package mcdodik.springai.utils.reader

data class DocumentChunk(
    val content: String,
    val metadata: Map<String, Any?> = emptyMap()
)