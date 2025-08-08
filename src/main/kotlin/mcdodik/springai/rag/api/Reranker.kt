package mcdodik.springai.rag.api

import mcdodik.springai.rag.model.ScoredDoc
import org.springframework.ai.document.Document

interface Reranker {
    fun rerank(userEmbedding: FloatArray, raw: List<Document>): List<ScoredDoc>
    fun dedup(scored: List<ScoredDoc>): List<ScoredDoc>
}