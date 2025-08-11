package mcdodik.springai.advisors

import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor

class PostRequestAdvisor : BaseAdvisor {
    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain,
    ): ChatClientRequest {
        return chatClientRequest
    }

    override fun after(
        chatClientResponse: ChatClientResponse,
        advisorChain: AdvisorChain,
    ): ChatClientResponse {
        TODO("Проверить на галюны, повторы слов и если что, то зафейлить ответ")
    }

    override fun getOrder(): Int {
        return 50
    }
}
