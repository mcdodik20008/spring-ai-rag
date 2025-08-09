package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.rag.Bm25Row
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface Bm25Mapper {
    fun search(@Param("q") q: String, @Param("topK") topK: Int): List<Bm25Row>
}