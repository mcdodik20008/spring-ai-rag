package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.model.rag.RagChunkEntity
import org.apache.ibatis.annotations.Mapper

/**
 * Mapper interface for interacting with the RagChunk database table using MyBatis.
 * Provides methods for inserting document chunks and performing similarity searches based on embedding vectors.
 */
@Mapper
interface RagChunkMapper {

    /**
     * Inserts a single document chunk into the database.
     *
     * @param chunk The [RagChunkEntity] representing the document chunk to be inserted.
     */
    fun insert(chunk: RagChunkEntity)

    /**
     * Performs a filtered similarity search in the database using an embedding vector.
     * Returns a list of document chunks that match the given criteria.
     *
     * @param embedding List of floats representing the embedding vector used for similarity search.
     * @param similarityThreshold Minimum similarity score required for a match (higher values mean more similarity).
     * @param topK Maximum number of results to return.
     * @param filterClause Optional SQL filter clause to further narrow down the search results.
     * @return A list of [RagChunkEntity] objects that match the search criteria.
     */
    fun searchByEmbeddingFiltered(
        embedding: List<Float>,
        similarityThreshold: Double?,
        topK: Int?,
        filterClause: String?
    ): List<RagChunkEntity>
}
