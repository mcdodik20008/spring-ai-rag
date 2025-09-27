package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.user.ChatMessageRecord
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface ChatMessageMapper {
    fun findByConversationId(
        @Param("conversationId") conversationId: Long,
    ): List<ChatMessageRecord>

    fun insert(record: ChatMessageRecord)

    fun deleteByConversationId(
        @Param("conversationId") conversationId: Long,
    )
}
