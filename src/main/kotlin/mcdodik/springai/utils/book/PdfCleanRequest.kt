package mcdodik.springai.utils.book

data class PdfCleanRequest(
    val skipPages: Int = 20,
    val headerFooterLines: Int = 2,
    val repeatThreshold: Double = 0.8
)