package mcdodik.springai.apiai.v1.controller

import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.chat.ChatResponseDto
import mcdodik.springai.apiai.v1.serivces.RunsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/runs")
class RunsController(
    private val runsService: RunsService,
) {
    @GetMapping("/{runId}")
    fun get(
        @PathVariable runId: String,
    ): Mono<ChatResponseDto> = runsService.get(runId)

    @GetMapping
    fun list(
        @RequestParam("kb_id", required = false) kbId: String?,
        @RequestParam("from", required = false) from: String?,
        @RequestParam("to", required = false) to: String?,
        @RequestParam("limit", defaultValue = "50") limit: Int,
        @RequestParam("offset", defaultValue = "0") offset: Int,
    ): Mono<PageDto<ChatResponseDto>> = runsService.list(kbId, from, to, limit, offset)
}
