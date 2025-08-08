package mcdodik.springai.rag.api

import org.springframework.ai.document.Document

interface ContextBuilder {
    fun build(docs: List<Document>, maxChars: Int): String
}
