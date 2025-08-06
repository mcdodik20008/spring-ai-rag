package mcdodik.springai.api.controller

import mcdodik.springai.api.controller.model.AskRequest
import mcdodik.springai.rag.service.RagService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/ask")
class AskController(
    private val rag: RagService,
    @Qualifier("openRouterChatClient")
    private val summarizer: ChatClient,
) {

    @PostMapping
    suspend fun ask(@RequestBody req: AskRequest): Flux<String> {
        return rag.ask(req.question)
    }

    @GetMapping
    suspend fun summarize(@RequestParam quest: String): Flux<String> {
        return summarizer.prompt(quest).stream().content()
    }

}