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

/**
 * REST controller for handling question-answering and summarization requests.
 *
 * @param rag Service responsible for retrieving answers using RAG (Retrieval-Augmented Generation)
 * @param summarizer Chat client used for summarizing content. This client is specifically named "openRouterChatClient".
 */
@RestController
@RequestMapping("/api/ask")
class AskController(
    private val rag: RagService,
    @Qualifier("openRouterChatClient")
    private val summarizer: ChatClient,
) {

    /**
     * Handles POST requests to the "/api/ask" endpoint.
     * Accepts a JSON body with a question and returns a stream of answer chunks.
     *
     * @param req The request object containing the question.
     * @return A [Flux] emitting string chunks of the generated answer.
     */
    @PostMapping
    suspend fun ask(@RequestBody req: AskRequest): Flux<String> {
        return rag.ask(req.question)
    }

    /**
     * Handles GET requests to the "/api/ask" endpoint.
     * Accepts a query parameter "quest" representing the input text to summarize.
     * Returns a stream of summary chunks.
     *
     * @param quest The input text to be summarized.
     * @return A [Flux] emitting string chunks of the generated summary.
     */
    @GetMapping
    suspend fun summarize(@RequestParam quest: String): Flux<String> {
        return summarizer.prompt(quest).stream().content()
    }

}