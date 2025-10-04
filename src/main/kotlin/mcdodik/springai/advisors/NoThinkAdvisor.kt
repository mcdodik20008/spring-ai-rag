package mcdodik.springai.advisors

import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.prompt.Prompt

class NoThinkAdvisor : BaseAdvisor {
    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain,
    ): ChatClientRequest {
        val original = chatClientRequest.prompt

        val alreadyHasRu = original.instructions.any { it.text.contains("Не рассуждай", ignoreCase = true) }

        val newMessages =
            buildList {
                addAll(original.instructions)
                if (!alreadyHasRu) {
                    add(SystemMessage("Не рассуждай, не показывай промежуточные шаги. Дай сразу ответ."))
                }
            }

        val mutatedPrompt = Prompt(newMessages)

        return chatClientRequest
            .mutate()
            .prompt(mutatedPrompt)
            .build()
    }

    override fun after(
        chatClientResponse: ChatClientResponse,
        advisorChain: AdvisorChain,
    ): ChatClientResponse = chatClientResponse

    override fun getOrder(): Int = 40
}
