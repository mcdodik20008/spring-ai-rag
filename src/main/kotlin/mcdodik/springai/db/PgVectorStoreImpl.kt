package mcdodik.springai.db

import mcdodik.springai.config.Loggable
import mcdodik.springai.db.entity.rag.MetadataKey
import mcdodik.springai.db.entity.rag.RagChunkEntity
import mcdodik.springai.db.mybatis.mapper.RagChunkMapper
import mcdodik.springai.extensions.toFilterClause
import org.springframework.ai.document.Document
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.Filter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Implementation of the VectorStore interface for PostgreSQL-based vector storage.
 * Provides methods to store and retrieve documents using vector similarity search.
 * This implementation is annotated with @Qualifier("customPgVectorStore") to distinguish it from other implementations.
 */
@Component
@Qualifier("customPgVectorStore")
class PgVectorStoreImpl(
    private val embeddingModel: OllamaEmbeddingModel,
    private val ragChunkMapper: RagChunkMapper
) : VectorStore {

    override fun write(documents: List<Document>) {
        for (it in documents) {
            it.metadata[MetadataKey.EMBEDDING.key] = embeddingModel.embed(it.text ?: "empty").toList()
            ragChunkMapper.insert(RagChunkEntity.from(it))
        }
    }

    override fun similaritySearch(request: SearchRequest): List<Document> {
        val embedding = embeddingModel.embed(request.query).toList()
        logger.debug("Searching for {}", embedding)

        val result = ragChunkMapper.searchByEmbeddingFiltered(
            embedding = embedding,
            similarityThreshold = request.similarityThreshold,
            topK = request.topK
        )

        logger.debug("Found chunk with ids: {}", result.map { it.id })

        return result.map {
            Document(
                it.content, mapOf(
                    MetadataKey.ID.key to it.id,
                    MetadataKey.EMBEDDING.key to it.embedding,
                    MetadataKey.TYPE.key to it.type,
                    MetadataKey.SOURCE.key to it.source,
                    MetadataKey.CHUNK_INDEX.key to it.chunkIndex,
                    MetadataKey.FILE_NAME.key to it.fileName,
                    MetadataKey.EXTENSION.key to it.extension,
                    MetadataKey.HASH.key to it.hash
                )
            )
        }
    }

    /**
     * Adds a list of documents to the vector store.
     * Currently not implemented beyond a placeholder message.
     *
     * @param documents List of documents to add (can be null).
     */
    override fun add(documents: List<Document?>) {
        print("Спасибо, работаем братья!")
    }

    /**
     * Deletes documents from the vector store by their IDs.
     * Currently not implemented beyond a placeholder message.
     *
     * @param idList List of document IDs to delete (can be null).
     */
    override fun delete(idList: List<String?>) {
        print("Спасибо, работаем братья!")
    }

    /**
     * Deletes documents from the vector store based on a filter expression.
     * Currently not implemented beyond a placeholder message.
     *
     * @param filterExpression Filter criteria for deletion.
     */
    override fun delete(filterExpression: Filter.Expression) {
        print("Спасибо, работаем братья!")
    }

    /**
     * Companion object that provides access to a logger for this class.
     * Implements the Loggable interface to simplify logging.
     */
    companion object : Loggable
}
