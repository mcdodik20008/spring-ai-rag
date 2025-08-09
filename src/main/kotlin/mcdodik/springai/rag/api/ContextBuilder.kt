package mcdodik.springai.rag.api

import mcdodik.springai.rag.model.RetrievedDoc

interface ContextBuilder {
    fun build(docs: List<RetrievedDoc>, maxChars: Int): String
}
