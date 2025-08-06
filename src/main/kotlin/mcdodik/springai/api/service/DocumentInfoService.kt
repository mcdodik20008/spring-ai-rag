package mcdodik.springai.api.service

import java.util.UUID
import mcdodik.springai.db.model.DocumentInfo
import mcdodik.springai.db.model.DocumentMetadataKey
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import org.apache.ibatis.javassist.NotFoundException
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class DocumentInfoService(
    @Qualifier("customPgVectorStore")
    private val vectorStore: VectorStore,
    private val documentStore: DocumentInfoMapper,
) {

    fun searchDocumentsByVector(query: String, topK: Int = 5): List<DocumentInfo> {
        val similarChunks = vectorStore.similaritySearch(query)

        // 3. Получаем уникальные ID документов
        val fileNames = similarChunks
            .mapNotNull { it.metadata[DocumentMetadataKey.FILE_NAME.key] as String }
            .distinct()

        // 4. Получаем документы по ID
        return fileNames.take(5).map { documentStore.findByFileName(it) }
    }

    fun getAll(): List<DocumentInfo> = documentStore.findAll()

    fun getById(id: UUID): DocumentInfo =
        documentStore.findById(id) ?: throw NotFoundException("Документ $id не найден")

    fun getByFileName(fileName: String): DocumentInfo {
        return documentStore.findByFileName(fileName)
    }


    //    fun create(doc: DocumentInfo): DocumentInfo {
//        val newDoc = doc.copy(id = UUID.randomUUID(), createdAt = LocalDateTime.now())
//        mapper.insert(newDoc)
//        return newDoc
//    fun update(id: UUID, updated: DocumentInfo): DocumentInfo {
//        val existing = mapper.findById(id) ?: throw NotFoundException("Документ $id не найден")
//        val toSave = updated.copy(id = existing.id, createdAt = existing.createdAt)
//        mapper.update(toSave)
//        return toSave

//    }

//    }

    fun delete(id: UUID) {
        documentStore.delete(id)
    }
}
