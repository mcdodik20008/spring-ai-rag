package mcdodik.springai.apiai.v1.serivces

import mcdodik.springai.apiai.v1.dto.ChatResponseDto
import mcdodik.springai.apiai.v1.dto.PageDto
import reactor.core.publisher.Mono

interface RunsService {
    fun get(runId: String): Mono<ChatResponseDto>

    fun list(
        kbId: String?,
        from: String?,
        to: String?,
        limit: Int,
        offset: Int,
    ): Mono<PageDto<ChatResponseDto>>
}
