package mcdodik.springai.controller

import mcdodik.springai.model.AskRequest
import mcdodik.springai.model.AskResponse
import mcdodik.springai.service.RagService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ask")
class AskController(
    private val rag: RagService,
    @Qualifier("openRouterChatClient") private val summarizer: ChatClient,
) {

    @PostMapping
    fun ask(@RequestBody req: AskRequest): AskResponse = AskResponse(rag.ask(req.question))

    @GetMapping
    fun summarize(@RequestParam quest: String): String {
        return summarizer.prompt(quest).call().content().toString()
    }

}