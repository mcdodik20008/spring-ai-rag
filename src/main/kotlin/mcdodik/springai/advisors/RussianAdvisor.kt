package mcdodik.springai.advisors

import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.prompt.Prompt

class RussianAdvisor : BaseAdvisor {
    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain,
    ): ChatClientRequest {
        val original = chatClientRequest.prompt

        val alreadyHasRu = original.instructions.any { it.text.contains("Ответь на русском", ignoreCase = true) }

        val newMessages =
            buildList {
                addAll(original.instructions)
                if (!alreadyHasRu) {
                    add(SystemMessage("Ответь на русском"))
                }
            }

        val mutatedPrompt = Prompt(newMessages)

        return chatClientRequest
            .mutate()
            .prompt(mutatedPrompt)
            .build()
    }

    override fun after(
        chatClientRequest: ChatClientResponse,
        advisorChain: AdvisorChain,
    ): ChatClientResponse = chatClientRequest

    override fun getOrder(): Int = 1000
}
