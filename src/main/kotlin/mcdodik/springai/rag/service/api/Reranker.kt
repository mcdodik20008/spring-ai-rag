package mcdodik.springai.rag.service.api

import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoredDoc

interface Reranker {
    fun rerank(
        userEmbedding: FloatArray,
        raw: List<RetrievedDoc>,
    ): List<ScoredDoc>

    fun dedup(scored: List<ScoredDoc>): List<ScoredDoc>
}
