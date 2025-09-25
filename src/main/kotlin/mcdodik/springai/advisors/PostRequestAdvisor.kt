package mcdodik.springai.advisors

import io.mockk.core.ValueClassSupport.boxedValue
import mcdodik.springai.config.Loggable
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.AdvisorChain
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor

class PostRequestAdvisor :
    BaseAdvisor,
    Loggable {
    override fun before(
        chatClientRequest: ChatClientRequest,
        advisorChain: AdvisorChain,
    ): ChatClientRequest = chatClientRequest

    override fun after(
        chatClientResponse: ChatClientResponse,
        advisorChain: AdvisorChain,
    ): ChatClientResponse {
        val text =
            chatClientResponse.chatResponse!!
                .result.output.text ?: ""
        logger
            .atInfo()
            .addKeyValue("event", "llm_response")
            .addKeyValue("advisor", this::class.simpleName)
            .addKeyValue("response_chars", text.length)
            .addKeyValue(
                "boxed_value",
                chatClientResponse.chatResponse!!
                    .result.output.boxedValue,
            ).log("LLM response")

        return chatClientResponse
    }

    override fun getOrder(): Int = 50
}
