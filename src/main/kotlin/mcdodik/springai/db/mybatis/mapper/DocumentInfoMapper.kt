package mcdodik.springai.db.mybatis.mapper

import java.util.UUID
import mcdodik.springai.db.model.DocumentInfo
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface DocumentInfoMapper {

    fun findAll(): List<DocumentInfo>

    fun findById(id: UUID): DocumentInfo?

    fun insert(documentInfo: DocumentInfo)

    fun findByFileName(fileName: String): DocumentInfo

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
