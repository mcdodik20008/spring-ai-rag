package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.model.RagChunkEntity
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface RagChunkMapper {

    fun insert(chunk: RagChunkEntity)

    fun searchByEmbeddingFiltered(
        embedding: List<Float>,
        similarityThreshold: Double,
        topK: Int,
        filterClause: String?
    ): List<RagChunkEntity>

}