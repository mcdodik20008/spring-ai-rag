package mcdodik.springai.rag.application

import mcdodik.springai.rag.api.Retriever
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore

class VectorStoreRetriever(
    private val vectorStore: VectorStore,
) : Retriever {
    override fun retrieve(query: String, topK: Int, threshold: Double): List<Document> =
        vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build()
        )
}