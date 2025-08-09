package mcdodik.springai.rag.application

import mcdodik.springai.extensions.toRetrievedDoc
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoreType
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore

class VectorStoreRetriever(
    private val vectorStore: VectorStore,
) : Retriever {


    override fun retrieve(query: String, topK: Int, threshold: Double?): List<RetrievedDoc> =
        vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold ?: 0.0)
                .build()
        ).map { it.toRetrievedDoc(ScoreType.VECTOR) }
}
