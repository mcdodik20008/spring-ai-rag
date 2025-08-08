package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.prompt.ChunkingPromptTemplate
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface ChunkingPromptTemplateMapper {

    fun insert(template: ChunkingPromptTemplate)

    fun findAll(): List<ChunkingPromptTemplate>

    fun searchByTopicEmbedding(
        @Param("queryEmbedding") queryEmbedding: List<Float>,
        @Param("k") k: Int
    ): List<ChunkingPromptTemplate>

    fun searchByTopicLike(
        @Param("topic") topic: String,
        @Param("k") k: Int
    ): List<ChunkingPromptTemplate>
}
