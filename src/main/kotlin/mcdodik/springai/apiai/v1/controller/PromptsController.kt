package mcdodik.springai.apiai.v1.controller

import jakarta.validation.Valid
import mcdodik.springai.apiai.v1.dto.PageDto
import mcdodik.springai.apiai.v1.dto.prompt.PromptDto
import mcdodik.springai.apiai.v1.serivces.PromptsService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/prompts")
class PromptsController(
    private val promptsService: PromptsService,
) {
    @GetMapping
    fun list(
        @RequestParam("type", required = false) type: String?,
        @RequestParam("limit", defaultValue = "50") limit: Int,
        @RequestParam("offset", defaultValue = "0") offset: Int,
    ): Mono<PageDto<PromptDto>> = promptsService.list(type, limit, offset)

    @PostMapping
    fun create(
        @Valid @RequestBody prompt: PromptDto,
    ): Mono<PromptDto> = promptsService.create(prompt)

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: String,
    ): Mono<PromptDto> = promptsService.get(id)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody prompt: PromptDto,
    ): Mono<PromptDto> = promptsService.update(id, prompt)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String,
    ): Mono<Void> = promptsService.delete(id)
}
