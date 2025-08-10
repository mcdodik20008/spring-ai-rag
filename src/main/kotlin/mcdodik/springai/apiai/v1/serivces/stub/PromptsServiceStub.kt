package mcdodik.springai.apiai.v1.serivces.stub

import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.PromptDto
import mcdodik.springai.apiai.v1.dto.PromptType
import mcdodik.springai.apiai.v1.serivces.PromptsService
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Service
@Primary
@Profile("stub")
class PromptsServiceStub : PromptsService {
    override fun list(
        type: String?,
        limit: Int,
        offset: Int,
    ): Mono<PageDto<PromptDto>> = Mono.just(PageDto(items = emptyList(), total = 0, limit = limit, offset = offset))

    override fun create(prompt: PromptDto): Mono<PromptDto> = Mono.just(prompt.copy(id = "pr_${UUID.randomUUID()}", version = 1, createdAt = Instant.now().toString()))

    override fun get(id: String): Mono<PromptDto> = Mono.just(PromptDto(id = id, name = "chunk_llm_v2", type = PromptType.CHUNKING, template = "stub"))

    override fun update(
        id: String,
        prompt: PromptDto,
    ): Mono<PromptDto> = Mono.just(prompt.copy(id = id))

    override fun delete(id: String): Mono<Void> = Mono.empty()
}
