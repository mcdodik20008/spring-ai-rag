package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.user.ConversationRecord
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface ConversationMapper {
    fun findById(
        @Param("id") id: Long,
    ): ConversationRecord?

    fun findByUserId(
        @Param("userId") userId: Long,
    ): List<ConversationRecord>

    /**
     * Вставляет новый диалог и обновляет поле 'id' в переданном объекте.
     */
    fun insert(conversation: ConversationRecord): Long
}
