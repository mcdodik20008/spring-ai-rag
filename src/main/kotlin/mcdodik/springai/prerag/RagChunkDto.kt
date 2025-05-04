package mcdodik.springai.prerag

data class RagChunkDto(
    val content: String,
    val type: String // "code", "text", "quote", "list", и т.п.
)
