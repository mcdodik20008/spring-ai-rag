package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.rag.DocumentInfo
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.util.UUID

/**
 * Mapper interface for interacting with the DocumentInfo database table using MyBatis.
 * Provides methods for CRUD operations and advanced searches on document metadata.
 */
@Mapper
interface DocumentInfoMapper {
    fun findAll(): List<DocumentInfo>

    fun findById(
        @Param("id") id: UUID,
    ): DocumentInfo?

    fun insert(
        @Param("documentInfo") documentInfo: DocumentInfo,
    )

    fun searchByFilenames(
        @Param("fileNames") fileNames: Set<String>,
    ): List<DocumentInfo>

    fun searchByFilenameLike(
        @Param("fileName") fileName: String,
    ): List<DocumentInfo>

    fun searchByNameAndHash(
        @Param("fileName") fileName: String,
        @Param("hash") hash: String,
    ): DocumentInfo?

    fun delete(
        @Param("id") id: UUID,
    )
}
