package mcdodik.springai.rag.service.api

import mcdodik.springai.rag.model.RetrievedDoc

interface Retriever {
    fun retrieve(
        query: String,
        topK: Int,
        threshold: Double? = null,
    ): List<RetrievedDoc>
}
