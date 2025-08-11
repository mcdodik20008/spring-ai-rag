package mcdodik.springai.apiai.v1.serivces

import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.prompt.PromptDto
import reactor.core.publisher.Mono

interface PromptsService {
    fun list(
        type: String?,
        limit: Int,
        offset: Int,
    ): Mono<PageDto<PromptDto>>

    fun create(prompt: PromptDto): Mono<PromptDto>

    fun get(id: String): Mono<PromptDto>

    fun update(
        id: String,
        prompt: PromptDto,
    ): Mono<PromptDto>

    fun delete(id: String): Mono<Void>
}
