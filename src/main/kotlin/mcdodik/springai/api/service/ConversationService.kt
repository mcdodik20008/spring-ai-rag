package mcdodik.springai.api.service

import mcdodik.springai.api.dto.user.ConversationResponse
import mcdodik.springai.db.mybatis.mapper.ConversationMapper
import org.springframework.stereotype.Service

@Service
class ConversationService(
    private val conversationMapper: ConversationMapper,
) {
    fun findConversationsByUserId(userId: Long): List<ConversationResponse> =
        conversationMapper.findByUserId(userId).map {
            ConversationResponse(
                id = it.id!!,
                userId = it.userId,
                title = it.title,
            )
        }
}
