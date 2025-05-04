package mcdodik.springai.utils.embeded

interface Embedder {
    fun embed(text: String): List<Float>
}
