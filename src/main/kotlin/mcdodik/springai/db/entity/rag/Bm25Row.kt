package mcdodik.springai.db.entity.rag

data class Bm25Row(
    val id: String,
    val content: String,
    val score: Double,
)
