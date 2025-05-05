package mcdodik.springai.retrival

import org.springframework.ai.document.Document

interface CustomRetrieval {
    fun retrieve(query: String): List<Document>
}
