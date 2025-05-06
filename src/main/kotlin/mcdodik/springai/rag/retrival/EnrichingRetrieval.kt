package mcdodik.springai.rag.retrival

import org.springframework.ai.document.Document
import org.springframework.stereotype.Component

@Component
class EnrichingRetrieval : CustomRetrieval {

    override fun retrieve(query: String): List<Document> {
        val additionalContext = fetchAdditionalContext(query)

        return additionalContext.map {
            Document(
                it,
                mapOf("source" to "custom-db")
            )
        }
    }

    private fun fetchAdditionalContext(query: String): List<String> {
        TODO("Делаем вещи брат, но пока не готово")
    }
}
