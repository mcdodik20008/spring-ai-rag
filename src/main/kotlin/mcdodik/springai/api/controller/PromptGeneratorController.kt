package mcdodik.springai.api.controller

import mcdodik.springai.api.service.PromptGenerationService
import mcdodik.springai.db.entity.prompt.ChunkingPromptTemplate
import mcdodik.springai.db.entity.prompt.PromptGenerationRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompt")
class PromptGeneratorController(
    private val service: PromptGenerationService
) {

    @PostMapping("/generate")
    fun generatePrompt(@RequestBody request: PromptGenerationRequest): ChunkingPromptTemplate {
        return service.generatePrompt(request.domainName, request.userDescription)
    }
}