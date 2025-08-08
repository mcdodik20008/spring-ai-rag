package mcdodik.springai.rag.api

import org.springframework.ai.document.Document

interface Retriever {
    fun retrieve(query: String, topK: Int, threshold: Double): List<Document>
}