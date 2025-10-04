package mcdodik.springai.chatmem

import groovyjarjarantlr4.v4.tool.ToolMessage
import mcdodik.springai.db.entity.user.ChatMessageRow
import mcdodik.springai.db.mybatis.mapper.ChatMessageMapper
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("dbChatMemory")
class DbChatMemory(
    private val mapper: ChatMessageMapper,
    private val props: Props = Props(),
) : ChatMemory {
    data class Props(
        val windowSize: Int = 20,
    )

    override fun add(
        conversationId: String,
        messages: MutableList<Message>,
    ) {
        val convId =
            conversationId.toLongOrNull()
                ?: error("conversationId must be numeric (conversations.id), got=$conversationId")

        messages.forEach {
            val (messageType, text) = toDb(it)

            mapper.insert(
                ChatMessageRow(
                    conversationId = convId,
                    messageType = messageType,
                    content = text,
                ),
            )
            trimIfNeeded(convId)
        }
    }

    override fun get(conversationId: String): MutableList<Message> {
        val convId =
            conversationId.toLongOrNull()
                ?: error("conversationId must be numeric (conversations.id), got=$conversationId")

        val rows = mapper.findLastN(convId, props.windowSize)
        return rows.map { fromDb(it) }.toMutableList()
    }

    override fun clear(conversationId: String) {
        // Можно добавить метод deleteByConversationId при желании.
        mapper.deleteOldestExceptLast(conversationId.toLong(), 0)
    }

    @Transactional
    fun trimIfNeeded(convId: Long) {
        val cnt = mapper.count(convId)
        if (cnt > props.windowSize) {
            mapper.deleteOldestExceptLast(convId, props.windowSize)
        }
    }

    private fun toDb(message: Message): Pair<String, String> {
        val text = message.text ?: ""

        val type =
            when (message) {
                is UserMessage -> "USER"
                is AssistantMessage -> "AI"
                is SystemMessage -> "USER"
                is ToolMessage -> "AI"
                else -> "USER"
            }
        return type to text
    }

    private fun fromDb(row: ChatMessageRow): Message {
        val text = row.content
        return when (row.messageType.uppercase()) {
            "USER" -> UserMessage(text)
            "AI" -> AssistantMessage(text)
            "SYSTEM" -> SystemMessage(text)
            else -> UserMessage(text)
        }
    }
}
