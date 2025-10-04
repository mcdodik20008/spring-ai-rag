package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.config.testcontainer.AbstractPgIT
import mcdodik.springai.db.entity.user.ChatMessageRecord
import mcdodik.springai.db.entity.user.ConversationRecord
import mcdodik.springai.db.entity.user.UserRecord
import org.junit.jupiter.api.BeforeEach
import org.mybatis.spring.annotation.MapperScan
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.ai.chat.messages.MessageType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@MybatisTest(excludeAutoConfiguration = [SessionAutoConfiguration::class])
@MapperScan("mcdodik.springai.db.mybatis.mapper")
class UserAndConversationMapperTest
    @Autowired
    constructor(
        private val userMapper: UserMapper,
        private val conversationMapper: ConversationMapper,
        private val chatMessageMapper: ChatMessageMapper,
        private val jdbc: JdbcTemplate,
    ) : AbstractPgIT() {
        @BeforeEach
        fun setup() {
            // Очищаем таблицы в правильном порядке, чтобы не нарушать foreign key constraints
            jdbc.execute("DELETE FROM chat_messages")
            jdbc.execute("DELETE FROM conversations")
            jdbc.execute("DELETE FROM users")
        }

        // --- Тесты для UserMapper ---

        @Test
        fun `insert and findByLogin successfully saves and retrieves a user`() {
            // Arrange
            val newUser = UserRecord(login = "testuser")

            // Act
            userMapper.insert(newUser)
            val foundUser = userMapper.findByLogin("testuser")

            // Assert
            assertNotNull(newUser.id, "ID should be populated by MyBatis")
            assertNotNull(foundUser)
            assertEquals("testuser", foundUser.login)
            assertEquals(newUser.id, foundUser.id)
        }

        @Test
        fun `findByLogin returns null for non-existent user`() {
            // Act
            val foundUser = userMapper.findByLogin("non_existent_user")

            // Assert
            assertNull(foundUser)
        }

        @Test
        fun `inserting a user with a duplicate login throws exception`() {
            // Arrange
            userMapper.insert(UserRecord(login = "duplicate_login"))

            // Act & Assert
            assertFailsWith<DuplicateKeyException> {
                userMapper.insert(UserRecord(login = "duplicate_login"))
            }
        }

        // --- Тесты для ConversationMapper ---

        @Test
        fun `insert and findById successfully saves and retrieves a conversation`() {
            // Arrange
            val user = createUser("user1")
            val newConversation = ConversationRecord(userId = user.id!!, title = "First chat")

            // Act
            conversationMapper.insert(newConversation)
            val foundConversation = conversationMapper.findById(newConversation.id!!)

            // Assert
            assertNotNull(newConversation.id)
            assertNotNull(foundConversation)
            assertEquals("First chat", foundConversation.title)
            assertEquals(user.id, foundConversation.userId)
        }

        @Test
        fun `findByUserId returns all conversations for a user`() {
            // Arrange
            val user1 = createUser("user_with_chats")
            val user2 = createUser("user_without_chats")

            conversationMapper.insert(ConversationRecord(userId = user1.id!!, title = "Chat 1"))
            conversationMapper.insert(ConversationRecord(userId = user1.id!!, title = "Chat 2"))

            // Act
            val user1Conversations = conversationMapper.findByUserId(user1.id!!)
            val user2Conversations = conversationMapper.findByUserId(user2.id!!)

            // Assert
            assertEquals(2, user1Conversations.size)
            assertTrue(user2Conversations.isEmpty())
            assertTrue(user1Conversations.all { it.userId == user1.id })
        }

        // --- Тесты для ChatMessageMapper и связей ---

        @Test
        fun `insert and findByConversationId saves and retrieves messages correctly`() {
            // Arrange
            val conversation = createConversationForUser("user_for_messages")

            val userMessage =
                ChatMessageRecord(
                    conversationId = conversation.id!!,
                    messageType = MessageType.USER,
                    content = "Hello, AI!",
                )
            val aiMessage =
                ChatMessageRecord(
                    conversationId = conversation.id!!,
                    messageType = MessageType.ASSISTANT,
                    content = "Hello, User!",
                )

            // Act
            chatMessageMapper.insert(userMessage)
            chatMessageMapper.insert(aiMessage)
            val messages = chatMessageMapper.findByConversationId(conversation.id!!)

            // Assert
            assertEquals(2, messages.size)

            assertEquals(MessageType.USER, messages[0].messageType)
            assertEquals("Hello, AI!", messages[0].content)

            assertEquals(MessageType.ASSISTANT, messages[1].messageType)
            assertEquals("Hello, User!", messages[1].content)
        }

        @Test
        fun `deleteByConversationId removes messages`() {
            // Arrange
            val conversation = createConversationForUser("user_to_delete_messages")
            chatMessageMapper.insert(
                ChatMessageRecord(
                    conversationId = conversation.id!!,
                    messageType = MessageType.USER,
                    content = "Some message",
                ),
            )

            // Act
            val messagesBeforeDelete = chatMessageMapper.findByConversationId(conversation.id!!)
            chatMessageMapper.deleteByConversationId(conversation.id!!)
            val messagesAfterDelete = chatMessageMapper.findByConversationId(conversation.id!!)

            // Assert
            assertEquals(1, messagesBeforeDelete.size)
            assertTrue(messagesAfterDelete.isEmpty())
        }

        // --- Вспомогательные функции для чистоты тестов ---

        private fun createUser(login: String): UserRecord {
            val user = UserRecord(login = login)
            userMapper.insert(user)
            return user
        }

        private fun createConversationForUser(
            login: String,
            title: String = "Test Conversation",
        ): ConversationRecord {
            val user = createUser(login)
            val conversation = ConversationRecord(userId = user.id!!, title = title)
            conversationMapper.insert(conversation)
            return conversation
        }
    }
