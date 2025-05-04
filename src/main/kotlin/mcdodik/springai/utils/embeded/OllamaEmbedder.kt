package mcdodik.springai.utils.embeded

import org.springframework.stereotype.Component
import org.springframework.ai.ollama.OllamaEmbeddingModel

@Component
class OllamaEmbedder(
    private val embeddingModel: OllamaEmbeddingModel
) : Embedder {

    override fun embed(text: String): List<Float> {
        val result = embeddingModel.embed(text)
        return result.toList()
    }
}
