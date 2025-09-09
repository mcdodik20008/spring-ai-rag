package mcdodik.springai.apiai.v1.serivces.impl

import mcdodik.springai.apiai.v1.dto.chat.ChatOutputDto
import mcdodik.springai.apiai.v1.dto.chat.ChatRequestDto
import mcdodik.springai.apiai.v1.dto.chat.ChatResponseDto
import mcdodik.springai.apiai.v1.serivces.ChatService
import mcdodik.springai.config.Loggable
import mcdodik.springai.rag.service.api.RagService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Service
@Primary
@Qualifier("chatService")
class ChatServiceImpl(
    private val ragService: RagService,
) : ChatService,
    Loggable {
    override suspend fun complete(req: ChatRequestDto): ChatResponseDto {
        val runId = UUID.randomUUID().toString()
        val sessionId = req.sessionId ?: UUID.randomUUID().toString()
        val start = Instant.now()

        val userMessage =
            req.messages
                .lastOrNull()
                ?.content
                ?.trim()
                .orEmpty()
        if (userMessage.isBlank()) {
            logger.warn("complete: blank message | runId={}", runId)
            return ChatResponseDto(runId, sessionId, ChatOutputDto(content = ""), null, null, 0)
        }

        // Собираем поток в один ответ
        val buf = StringBuilder()
        ragService.askFlow(userMessage).collect { buf.append(it) }

        val latency = Duration.between(start, Instant.now()).toMillis()
        logger.debug("complete: done | runId={}, latency={}ms, bytes={}", runId, latency, buf.length)
        return ChatResponseDto(runId, sessionId, ChatOutputDto(content = buf.toString()), null, null, latency)
    }

    override fun stream(req: ChatRequestDto): Flow<ServerSentEvent<Any>> =
        channelFlow {
            val runId = UUID.randomUUID().toString()
            val sessionId = req.sessionId ?: UUID.randomUUID().toString()
            val start = Instant.now()
            val userMessage =
                req.messages
                    .lastOrNull()
                    ?.content
                    ?.trim()
                    .orEmpty()

            send(sse("start", runId, mapOf("runId" to runId, "sessionId" to sessionId)))

            if (userMessage.isBlank()) {
                send(sse("error", runId, mapOf("message" to "empty message")))
                send(done(runId, sessionId, "", start))
                close()
                return@channelFlow
            }

            val agg = StringBuilder()

            // Keepalive
            val keepalive =
                launch {
                    while (isActive) {
                        delay(15_000)
                        send(sse("keepalive", runId, mapOf("runId" to runId)))
                    }
                }

            try {
                ragService.askFlow(userMessage).collect { chunk ->
                    agg.append(chunk)
                    send(sse("delta", runId, chunk))
                }
                send(done(runId, sessionId, agg.toString(), start))
            } catch (e: Throwable) {
                logger.error("stream: error | runId={}", runId, e)
                send(sse("error", runId, mapOf("message" to (e.message ?: "unknown error"))))
                send(done(runId, sessionId, agg.toString(), start))
            } finally {
                keepalive.cancel()
                close()
            }
        }

    private fun sse(
        event: String,
        id: String,
        data: Any?,
    ) = ServerSentEvent
        .builder<Any>()
        .event(event)
        .id(id)
        .data(data ?: emptyMap<String, Any>())
        .build()

    private fun done(
        runId: String,
        sessionId: String,
        content: String,
        start: Instant,
    ): ServerSentEvent<Any> {
        val latency = Duration.between(start, Instant.now()).toMillis()
        val dto = ChatResponseDto(runId, sessionId, ChatOutputDto(content = content), null, null, latency)
        return sse("done", runId, dto)
    }
}
