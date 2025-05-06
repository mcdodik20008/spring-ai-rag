package mcdodik.springai.rag.retrival

import org.springframework.ai.document.Document

interface CustomRetrieval {
    fun retrieve(query: String): List<Document>
}
