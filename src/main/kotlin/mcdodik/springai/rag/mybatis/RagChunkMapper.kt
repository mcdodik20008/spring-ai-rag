package mcdodik.springai.rag.mybatis

import mcdodik.springai.rag.db.RagChunk
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface RagChunkMapper {

    fun insert(chunk: RagChunk)

    fun searchByEmbedding(
        @Param("embedding") embedding: List<Float>
    ): List<RagChunk>
}
