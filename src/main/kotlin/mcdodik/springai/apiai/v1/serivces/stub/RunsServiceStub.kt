package mcdodik.springai.apiai.v1.serivces.stub

import mcdodik.springai.apiai.v1.dto.ChatOutputDto
import mcdodik.springai.apiai.v1.dto.ChatResponseDto
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.UsageDto
import mcdodik.springai.apiai.v1.serivces.RunsService
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Primary
@Service
@Profile("stub")
class RunsServiceStub : RunsService {
    override fun get(runId: String): Mono<ChatResponseDto> =
        Mono.just(
            ChatResponseDto(
                runId,
                sessionId = "sess_1",
                output = ChatOutputDto(content = "stub run"),
                usage = UsageDto(1, 1, 2),
                latencyMs = 1,
            ),
        )

    override fun list(
        kbId: String?,
        from: String?,
        to: String?,
        limit: Int,
        offset: Int,
    ): Mono<PageDto<ChatResponseDto>> = Mono.just(PageDto(items = emptyList(), total = 0, limit = limit, offset = offset))
}
