package mcdodik.springai.rag.service.impl

import mcdodik.springai.db.mybatis.mapper.Bm25Mapper
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoreType
import mcdodik.springai.rag.service.api.Retriever
import org.springframework.stereotype.Component

@Component
class PostgresBm25Retriever(
    private val mapper: Bm25Mapper,
) : Retriever {
    override fun retrieve(
        query: String,
        topK: Int,
        threshold: Double?,
    ): List<RetrievedDoc> {
        if (query.isBlank()) return emptyList()
        val rows = mapper.search(query, topK)
        return rows.map {
            RetrievedDoc(
                id = it.id,
                content = it.content,
                score = it.score,
                type = ScoreType.BM25,
            )
        }
    }
}
