package mcdodik.springai.api.config

import mcdodik.springai.chatmem.DbChatMemory
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ChatConfiguration {
    @Bean
    @Primary
    fun chatMemoryDb(dbChatMemory: DbChatMemory): ChatMemory = dbChatMemory
}
