package mcdodik.springai.rag.model

data class RetrievedDoc(
    val id: String,
    val content: String,
    val metadata: Map<String, Any?> = emptyMap(),
    val score: Double,          // чем выше — тем лучше
    val type: ScoreType
)
