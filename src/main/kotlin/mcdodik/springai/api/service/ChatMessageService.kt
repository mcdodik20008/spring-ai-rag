package mcdodik.springai.api.service

import mcdodik.springai.api.dto.user.ChatMessageResponse
import mcdodik.springai.api.dto.user.ChatResponseBundle
import mcdodik.springai.db.entity.user.ChatMessageRow
import mcdodik.springai.db.entity.user.ConversationRecord
import mcdodik.springai.db.mybatis.mapper.ChatMessageMapper
import mcdodik.springai.db.mybatis.mapper.ConversationMapper
import mcdodik.springai.rag.service.api.RagService
import org.springframework.stereotype.Service
import kotlinx.coroutines.reactive.awaitSingle

@Service
class ChatMessageService(
    private val chatMessageMapper: ChatMessageMapper,
    private val conversationMapper: ConversationMapper,
    private val ragService: RagService,
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

    suspend fun createMessage(
        userId: Long,
        conversationId: Long,
        messageType: String,
        content: String,
    ): ChatResponseBundle {
        val finalConversationId =
            if (conversationId == 0L || conversationId == -1L) {
                val title = content.take(60).replace("\n", " ")
                val conv = ConversationRecord(userId = userId, title = title)
                conversationMapper.insert(conv)
                conv.id!!
            } else {
                conversationId
            }

        val userRow =
            ChatMessageRow(
                conversationId = finalConversationId,
                messageType = messageType,
                content = content,
            )
        chatMessageMapper.insert(userRow)

        val aiResponse =
            ragService
                .ask(question = content)
                .collectList()
                .awaitSingle()
                ?.joinToString("") ?: "Ошибка: нет ответа от модели"

        val aiRow =
            ChatMessageRow(
                conversationId = finalConversationId,
                messageType = "AI",
                content = aiResponse,
            )
        chatMessageMapper.insert(aiRow)

        return ChatResponseBundle(
            conversationId = finalConversationId,
            userMessage =
                ChatMessageResponse(
                    id = userRow.id ?: -1L,
                    conversationId = finalConversationId,
                    messageType = "USER",
                    content = content,
                ),
            aiMessage =
                ChatMessageResponse(
                    id = aiRow.id ?: -1L,
                    conversationId = finalConversationId,
                    messageType = "AI",
                    content = aiResponse,
                ),
        )
    }
}
