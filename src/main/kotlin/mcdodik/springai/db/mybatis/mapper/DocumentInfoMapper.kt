package mcdodik.springai.db.mybatis.mapper

import java.util.UUID
import mcdodik.springai.db.entity.rag.DocumentInfo
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 * Mapper interface for interacting with the DocumentInfo database table using MyBatis.
 * Provides methods for CRUD operations and advanced searches on document metadata.
 */
@Mapper
interface DocumentInfoMapper {

    fun findAll(): List<DocumentInfo>

    fun findById(id: UUID): DocumentInfo?

    fun insert(documentInfo: DocumentInfo)

    fun searchByFilenames(@Param("fileNames") fileNames: Set<String>): List<DocumentInfo>

    fun searchByFilenameLike(fileName: String): DocumentInfo

    fun searchByNameAndHash(
        @Param("fileName") fileName: String,
        @Param("hash") hash: String
    ): DocumentInfo?

    fun getIdByFileNameAndHash(
        @Param("fileName") fileName: String,
        @Param("hash") hash: String
    ): UUID?

    fun delete(id: UUID)
}
