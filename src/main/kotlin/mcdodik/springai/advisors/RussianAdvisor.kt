package mcdodik.springai.advisors

import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor

class RussianAdvisor : BaseAdvisor {
    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain
    ): ChatClientRequest {
        TODO("Not yet implemented")
    }

    override fun after(
        chatClientResponse: ChatClientResponse,
        advisorChain: AdvisorChain
    ): ChatClientResponse {
        TODO("Not yet implemented")
    }

    override fun getOrder(): Int = 1000
}
