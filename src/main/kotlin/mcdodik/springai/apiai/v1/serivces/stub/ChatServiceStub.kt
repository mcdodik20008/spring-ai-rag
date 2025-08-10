package mcdodik.springai.apiai.v1.serivces.stub

import mcdodik.springai.apiai.v1.dto.ChatOutputDto
import mcdodik.springai.apiai.v1.dto.ChatRequestDto
import mcdodik.springai.apiai.v1.dto.ChatResponseDto
import mcdodik.springai.apiai.v1.dto.RetrievalDiagnosticsDto
import mcdodik.springai.apiai.v1.dto.RetrievedItemDto
import mcdodik.springai.apiai.v1.dto.UsageDto
import mcdodik.springai.apiai.v1.serivces.ChatService
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.UUID

@Service
@Primary
@Profile("stub")
class ChatServiceStub : ChatService {
    override fun complete(req: ChatRequestDto): Mono<ChatResponseDto> =
        Mono.just(
            ChatResponseDto(
                runId = "run_${UUID.randomUUID()}",
                sessionId = req.sessionId,
                output = ChatOutputDto(content = "✅ STUB: ответ для «${req.messages.lastOrNull()?.content}»"),
                retrievalDiagnostics =
                    RetrievalDiagnosticsDto(
                        items =
                            listOf(
                                RetrievedItemDto("ch_1", 0.85, "doc_1", listOf("highlight…")),
                            ),
                        hybrid = mapOf("vector_weight" to 0.6, "bm25_weight" to 0.4),
                    ),
                usage = UsageDto(10, 5, 15),
                latencyMs = 42,
            ),
        )

    override fun stream(req: ChatRequestDto): Flux<ServerSentEvent<Any>> =
        Flux.concat(
            Flux.just(
                ServerSentEvent.builder<Any>().event("context")
                    .data(mapOf("topK" to 3)).build(),
            ),
            Flux.interval(Duration.ofMillis(150))
                .take(5)
                .map { i -> ServerSentEvent.builder<Any>().event("message").data(mapOf("delta" to "chunk $i ")).build() },
            Flux.just(
                ServerSentEvent.builder<Any>().event("done")
                    .data(mapOf("run_id" to "run_${UUID.randomUUID()}")).build(),
            ),
        )
}
