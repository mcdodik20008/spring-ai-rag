package mcdodik.springai.rag.db

import java.time.LocalDateTime
import java.util.UUID
import mcdodik.springai.config.Loggable
import mcdodik.springai.extension.toRagChunkDTO
import mcdodik.springai.rag.db.mybatis.mapper.RagChunkMapper
import org.springframework.ai.document.Document
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.Filter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("customPgVectorStore")
class PgVectorStoreImpl(
    private val embeddingModel: OllamaEmbeddingModel,
    private val ragChunkMapper: RagChunkMapper
) : VectorStore {

    override fun write(documents: List<Document>) {
        for (it in documents) {
            it.metadata["embedding"] = embeddingModel.embed(it.text ?: "empty").toList()
            ragChunkMapper.insert(RagChunkEntity.from(it))
        }
    }

    override fun similaritySearch(request: SearchRequest): List<Document> {
        val embedding = embeddingModel.embed(request.query).toList()
        logger.debug("Searching for {}", embedding)
        val result = ragChunkMapper.searchByEmbedding(embedding)
        logger.debug("Found chunk with ids^ {}", result.map { x -> x.id })
        return result.map { x -> Document(x.content, mapOf("BLOCK_TYPE" to x.type)) }
    }

    override fun add(documents: List<Document?>) {
        print("Спасибо, работаем братья!")
    }

    override fun delete(idList: List<String?>) {
        print("Спасибо, работаем братья!")
    }

    override fun delete(filterExpression: Filter.Expression) {
        print("Спасибо, работаем братья!")
    }

    companion object : Loggable
}
