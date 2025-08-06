package mcdodik.springai.rag.db.mybatis.mapper

import java.util.UUID
import mcdodik.springai.rag.db.DocumentInfo
import org.apache.ibatis.annotations.*

@Mapper
interface DocumentInfoMapper {

    fun insert(documentInfo: DocumentInfo)

    fun searchByNameAndHash(
        @Param("fileName") fileName: String,
        @Param("hash") hash: String
    ): DocumentInfo?

    fun getIdByFileNameAndHash(
        @Param("fileName") fileName: String,
        @Param("hash") hash: String
    ): UUID?
}
