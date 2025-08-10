package mcdodik.springai.apiai.v1.controller

import jakarta.validation.Valid
import mcdodik.springai.apiai.v1.dto.ChatRequestDto
import mcdodik.springai.apiai.v1.dto.ChatResponseDto
import mcdodik.springai.apiai.v1.serivces.ChatService
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Validated
@RestController
@RequestMapping("/v1/chat")
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping("/completions", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun complete(
        @Valid @RequestBody req: ChatRequestDto,
    ): Mono<ChatResponseDto> = chatService.complete(req)

    @PostMapping(
        "/completions:stream",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun stream(
        @Valid @RequestBody req: ChatRequestDto,
    ): Flux<ServerSentEvent<Any>> = chatService.stream(req)
}
