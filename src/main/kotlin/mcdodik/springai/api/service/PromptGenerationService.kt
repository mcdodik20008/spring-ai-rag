package mcdodik.springai.api.service

import java.time.LocalDateTime
import java.util.UUID
import mcdodik.springai.config.chatmodel.ChatModelPrompts
import mcdodik.springai.config.chatmodel.ChatModelsConfig.LLMTaskType
import mcdodik.springai.db.entity.prompt.ChunkingPromptTemplate
import mcdodik.springai.db.mybatis.mapper.ChunkingPromptTemplateMapper
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service

@Service
class PromptGenerationService(
    private val dynamicOpenRouterChatClient: (LLMTaskType?, String?) -> ChatClient,
    private val mapper: ChunkingPromptTemplateMapper
) {

    fun generatePrompt(domainName: String, userDescription: String): ChunkingPromptTemplate {
        val systemPrompt = ChatModelPrompts.generateChunkingPrompt(domainName, userDescription)

        val prompt = Prompt(
            listOf(
                SystemMessage(systemPrompt),
                UserMessage(userDescription)
            )
        )

        val chatClient = dynamicOpenRouterChatClient(LLMTaskType.PROMPT_GEN, null)
        val response = chatClient.prompt(prompt).call().content()

        val entity = ChunkingPromptTemplate(
            id = UUID.randomUUID(),
            domainName = domainName,
            userDescription = userDescription,
            generatedPrompt = response ?: "Sorry, no prompt generated.",
            createdAt = LocalDateTime.now()
        )

        mapper.insert(entity)
        return entity
    }
}
