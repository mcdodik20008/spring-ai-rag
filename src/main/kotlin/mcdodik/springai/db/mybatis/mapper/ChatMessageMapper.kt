package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.user.ChatMessageRow
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ChatMessageMapper {
    fun insert(row: ChatMessageRow): Int

    fun findLastN(
        conversationId: Long,
        limit: Int,
    ): List<ChatMessageRow>

    fun findByConversationId(conversationId: Long): List<ChatMessageRow>

    fun count(conversationId: Long): Long

    fun deleteOldestExceptLast(
        conversationId: Long,
        keepLast: Int,
    ): Int
}
