package mcdodik.springai.db

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface RagChunkMapper {


    @Insert(
        """
    INSERT INTO rag_chunks (id, content, embedding, type, source, chunk_index, created_at)
    VALUES (#{id, typeHandler=mcdodik.springai.db.UUIDTypeHandler},
            #{content},
            #{embedding, typeHandler=mcdodik.springai.db.FloatListTypeHandler},
            #{type}, #{source}, #{chunkIndex}, #{createdAt})
"""
    )
    fun insert(chunk: RagChunk)

    @Insert("<script>" +
            "INSERT INTO rag_chunks (id, content, embedding, type, source, chunk_index, created_at) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.id, typeHandler=mcdodik.springai.db.UUIDTypeHandler}, #{item.content}, #{item.embedding, typeHandler=mcdodik.springai.db.FloatListTypeHandler}, " +
            "#{item.type}, #{item.source}, #{item.chunkIndex}, #{item.createdAt})" +
            "</foreach>" +
            "</script>")
    fun insertBatch(list: List<RagChunk>)

    @Select("""
        SELECT id, content, embedding, type, source, chunk_index, created_at
        FROM rag_chunks
        ORDER BY embedding <-> CAST(#{embedding, typeHandler=mcdodik.springai.db.FloatListTypeHandler} AS vector)
        LIMIT 5
    """)
    fun searchByEmbedding(
        @Param("embedding") embedding: List<Float>
    ): List<RagChunk>
}
