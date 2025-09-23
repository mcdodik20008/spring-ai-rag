package mcdodik.springai.rag.model

data class ScoredDoc(
    val doc: RetrievedDoc,
    val score: Double,
)
