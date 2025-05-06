package mcdodik.springai.utils.cleaner

data class PdfCleanRequest(
    val skipPages: Int = 3,
    val throwPagesFromEnd: Int = 0,
    val headerFooterLines: Int = 2,
    val repeatThreshold: Double = 0.8
)