package mcdodik.springai.advisors

import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor
import org.springframework.ai.chat.prompt.Prompt

class RussianAdvisor : BaseAdvisor {
    override fun before(
        req: ChatClientRequest,
        chain: AdvisorChain,
    ): ChatClientRequest {
        val originalPrompt = req.prompt

        val allContent =
            buildString {
                originalPrompt.instructions.forEach { msg ->
                    appendLine(msg.text)
                }
            }

        val mutatedPrompt =
            Prompt
                .builder()
                .content("$allContent\n\nОтветь на русском")
                .build()

        return req.mutate().prompt(mutatedPrompt).build()
    }

    override fun after(
        req: ChatClientResponse,
        chain: AdvisorChain,
    ): ChatClientResponse = req

    override fun getOrder(): Int = 1000
}
