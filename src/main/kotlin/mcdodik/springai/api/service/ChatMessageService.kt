package mcdodik.springai.api.service

import mcdodik.springai.api.dto.user.ChatMessageResponse
import mcdodik.springai.db.mybatis.mapper.ChatMessageMapper
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chatMessageMapper: ChatMessageMapper,
) {
    fun findMessagesByConversationId(conversationId: Long): List<ChatMessageResponse> =
        chatMessageMapper.findByConversationId(conversationId).map {
            ChatMessageResponse(
                id = it.id!!,
                conversationId = it.conversationId,
                messageType = it.messageType,
                content = it.content,
            )
        }
}
