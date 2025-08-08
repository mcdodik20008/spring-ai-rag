package mcdodik.springai.db

import mcdodik.springai.config.Loggable
import mcdodik.springai.db.entity.rag.DocumentMetadataKey
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
    /**
     * Embedding model used to convert document text into embedding vectors.
     * Utilizes the OLLaMA embedding model for this purpose.
     */
    private val embeddingModel: OllamaEmbeddingModel,

    /**
     * Mapper for interacting with the RagChunk database table.
     * Used to insert and query document chunks stored in the database.
     */
    private val ragChunkMapper: RagChunkMapper
) : VectorStore {

    /**
     * Stores a list of documents in the vector store.
     * Each document's text is converted into an embedding vector using the embedding model,
     * and then saved to the database via the RagChunkMapper.
     *
     * @param documents List of documents to be stored.
     */
    override fun write(documents: List<Document>) {
        for (it in documents) {
            it.metadata[DocumentMetadataKey.EMBEDDING.key] = embeddingModel.embed(it.text ?: "empty").toList()
            ragChunkMapper.insert(RagChunkEntity.from(it))
        }
    }

    /**
     * Performs a similarity search on the stored documents based on a query.
     * The query is embedded into a vector, and the most similar documents are retrieved
     * using a filtered search with optional similarity threshold and top-k parameters.
     *
     * @param request SearchRequest object containing the query and search parameters.
     * @return A list of documents that match the query based on vector similarity.
     */
    override fun similaritySearch(request: SearchRequest): List<Document> {
        val embedding = embeddingModel.embed(request.query).toList()
        logger.debug("Searching for {}", embedding)

        val filterClause = request.filterExpression.toFilterClause()

        val result = ragChunkMapper.searchByEmbeddingFiltered(
            embedding = embedding,
            similarityThreshold = request.similarityThreshold,
            topK = request.topK,
            filterClause = filterClause
        )

        logger.debug("Found chunk with ids: {}", result.map { it.id })

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
