package mcdodik.springai.api.service

import mcdodik.springai.db.entity.rag.DocumentInfo
import mcdodik.springai.db.entity.rag.MetadataKey
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import org.apache.ibatis.javassist.NotFoundException
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service class for managing document information.
 * Provides operations such as searching documents by query, retrieving all documents,
 * fetching documents by ID or file name, and deleting documents.
 */
@Service
class DocumentInfoService(
    /**
     * A custom vector store used to perform similarity searches on document data.
     * This store is qualified with "customPgVectorStore".
     */
    @Qualifier("customPgVectorStore")
    private val vectorStore: VectorStore,
    /**
     * Repository for accessing document data stored in the database.
     * Used for querying and modifying document records.
     */
    private val documentStore: DocumentInfoMapper,
) {
    /**
     * Searches for documents based on a text query using vector similarity.
     *
     * @param query The search query string.
     * @param topK The number of top results to return (default is 5).
     * @return A list of [DocumentInfo] objects that match the query.
     */
    fun searchDocumentsByVector(
        query: String,
        topK: Int = 5,
    ): List<DocumentInfo> {
        val similarChunks = vectorStore.similaritySearch(query)
        val fileNames =
            similarChunks
                .mapNotNull { it.metadata[MetadataKey.FILE_NAME.key] as String }
                .distinct()

        return fileNames
            .take(if (topK == 0) DEFAULT_TOP_K else topK)
            .map { documentStore.searchByFilenameLike(it) }
            .flatten()
    }

    /**
     * Retrieves all documents from the repository.
     *
     * @return A list of all [DocumentInfo] objects.
     */
    fun getAll(): List<DocumentInfo> = documentStore.findAll()

    /**
     * Retrieves a document by its unique identifier.
     *
     * @param id The unique ID of the document.
     * @return The [DocumentInfo] object corresponding to the given ID.
     * @throws NotFoundException If no document is found with the provided ID.
     */
    fun getById(id: UUID): DocumentInfo = documentStore.findById(id) ?: throw NotFoundException("Document $id not found")

    /**
     * Retrieves a document by its file name.
     *
     * @param fileName The name of the file associated with the document.
     * @return The [DocumentInfo] object corresponding to the given file name.
     */
    fun getByFileName(fileName: String): List<DocumentInfo> = documentStore.searchByFilenameLike(fileName)

    /**
     * Deletes a document by its unique identifier.
     *
     * @param id The unique ID of the document to delete.
     */
    fun delete(id: UUID) {
        documentStore.delete(id)
    }

    companion object {
        const val DEFAULT_TOP_K = 5
    }
}
