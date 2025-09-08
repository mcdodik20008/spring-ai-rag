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
    @Qualifier("customPgVectorStore")
    private val vectorStore: VectorStore,
    private val documentStore: DocumentInfoMapper,
) {
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

    fun getAll(): List<DocumentInfo> = documentStore.findAll()

    fun getById(id: UUID): DocumentInfo = documentStore.findById(id) ?: throw NotFoundException("Document $id not found")

    fun getByFileName(fileName: String): List<DocumentInfo> = documentStore.searchByFilenameLike(fileName)

    fun delete(id: UUID) {
        documentStore.delete(id)
    }

    companion object {
        const val DEFAULT_TOP_K = 5
    }
}
