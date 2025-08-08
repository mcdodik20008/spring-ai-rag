package mcdodik.springai.api.controller

import mcdodik.springai.api.service.PromptGenerationService
import mcdodik.springai.db.model.prompt.ChunkingPromptTemplate
import mcdodik.springai.db.model.prompt.PromptGenerationRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class ChunkingPromptController(
    private val service: PromptGenerationService
) {

    @PostMapping("/generate")
    fun generatePrompt(@RequestBody request: PromptGenerationRequest): ChunkingPromptTemplate {
        return service.generatePrompt(request.domainName, request.userDescription)
    }
}
