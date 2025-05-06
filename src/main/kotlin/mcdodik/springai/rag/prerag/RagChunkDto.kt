package mcdodik.springai.rag.prerag

data class RagChunkDto(
    val content: String,
    val type: String // "code", "text", "quote", "list", и т.п.
)
