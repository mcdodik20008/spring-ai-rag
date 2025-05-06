package mcdodik.springai.rag.retrival

import org.springframework.ai.document.Document
import org.springframework.stereotype.Service

@Service
class PromptAugmentationService(
    private val retrieval: CustomRetrieval
) {

    fun getAugmentedPrompt(query: String): String {
        val retrievedDocs = retrieval.retrieve(query)
        val augmentedContext = buildContext(retrievedDocs)

        return """
            $augmentedContext
            
            Вопрос пользователя: $query
        """.trimIndent()
    }

    private fun buildContext(docs: List<Document>): String =
        docs.joinToString("\n") { "- ${it.text}" }
}
