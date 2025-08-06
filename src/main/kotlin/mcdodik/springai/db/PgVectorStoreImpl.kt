package mcdodik.springai.db

import mcdodik.springai.config.Loggable
import mcdodik.springai.db.model.DocumentMetadataKey
import mcdodik.springai.db.model.RagChunkEntity
import mcdodik.springai.db.mybatis.mapper.RagChunkMapper
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
            it.metadata[DocumentMetadataKey.EMBEDDING.key] = embeddingModel.embed(it.text ?: "empty").toList()
            ragChunkMapper.insert(RagChunkEntity.from(it))
        }
    }

    override fun similaritySearch(request: SearchRequest): List<Document> {
        val embedding = embeddingModel.embed(request.query).toList()
        logger.debug("Searching for {}", embedding)
        val result = ragChunkMapper.searchByEmbedding(embedding)
        logger.debug("Found chunk with ids^ {}", result.map { x -> x.id })

        return result.map {
            Document(
                it.content, mapOf(
                    DocumentMetadataKey.EMBEDDING.key to it.embedding,
                    DocumentMetadataKey.TYPE.key to it.type,
                    DocumentMetadataKey.SOURCE.key to it.source,
                    DocumentMetadataKey.CHUNK_INDEX.key to it.chunkIndex,
                    DocumentMetadataKey.FILE_NAME.key to it.fileName,
                    DocumentMetadataKey.EXTENSION.key to it.extension,
                    DocumentMetadataKey.HASH.key to it.hash
                )
            )
        }
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
