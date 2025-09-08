package mcdodik.springai.scheduling.mapper

import mcdodik.springai.scheduling.model.ChunkForDedup
import mcdodik.springai.scheduling.model.ChunkForTfidf
import mcdodik.springai.scheduling.model.ChunkTfidfUpdate
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.util.UUID

@Mapper
interface TfidfMapper {
    fun selectChunksNeedingTfidf(
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<ChunkForTfidf>

    fun updateTfidf(
        @Param("u") update: ChunkTfidfUpdate,
    ): Int

    fun selectAllWithTfidf(
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<ChunkForDedup>

    fun findCandidatesByAnyTerms(
        @Param("id") id: UUID,
        @Param("terms") terms: List<String>,
        @Param("limit") limit: Int,
    ): List<ChunkForDedup>

    fun upsertDuplicate(
        @Param("dupId") dupId: UUID,
        @Param("keepId") keepId: UUID,
        @Param("sim") simScore: Double,
    ): Int
}
