package mcdodik.springai.apiai.v1.dto

data class RetrievalDiagnosticsDto(
    val items: List<RetrievedItemDto> = emptyList(),
    val hybrid: Map<String, Any?>? = null,
)
