package mcdodik.springai.db

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface RagChunkMapper {


    @Insert(
        """
    INSERT INTO rag_chunks (id, content, embedding, type, source, chunk_index, created_at, summary)
    VALUES (#{id, typeHandler=mcdodik.springai.db.UUIDTypeHandler},
            #{content},
            #{embedding, typeHandler=mcdodik.springai.db.FloatListTypeHandler},
            #{type},
            #{source},
            #{chunkIndex},
            #{createdAt},
            #{summary})
    """
    )
    fun insert(chunk: RagChunk)

    @Select(
        """
    SELECT id, content, embedding, type, source, chunk_index, created_at, summary
    FROM rag_chunks
    ORDER BY embedding <-> CAST(#{embedding, typeHandler=mcdodik.springai.db.FloatListTypeHandler} AS vector)
    LIMIT 5
    """
    )
    fun searchByEmbedding(
        @Param("embedding") embedding: List<Float>
    ): List<RagChunk>
}
