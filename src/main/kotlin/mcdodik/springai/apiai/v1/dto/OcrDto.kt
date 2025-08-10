package mcdodik.springai.apiai.v1.dto

data class OcrDto(
    val enabled: Boolean = false,
    val lang: List<String> = emptyList(),
)
