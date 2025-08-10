package mcdodik.springai.apiai.v1.serivces

import mcdodik.springai.apiai.v1.dto.ChatRequestDto
import mcdodik.springai.apiai.v1.dto.ChatResponseDto
import org.springframework.http.codec.ServerSentEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ChatService {
    fun complete(req: ChatRequestDto): Mono<ChatResponseDto>

    fun stream(req: ChatRequestDto): Flux<ServerSentEvent<Any>>
}
