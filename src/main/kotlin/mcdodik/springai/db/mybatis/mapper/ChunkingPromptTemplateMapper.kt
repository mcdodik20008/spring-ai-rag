package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.model.prompt.ChunkingPromptTemplate
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ChunkingPromptTemplateMapper {

    fun insert(template: ChunkingPromptTemplate)

    fun findAll(): List<ChunkingPromptTemplate>
}
