package mcdodik.springai.api.controller

import mcdodik.springai.api.dto.FindByTopicRequest
import mcdodik.springai.api.dto.FindByTopicResponse
import mcdodik.springai.api.service.PromptGenerationService
import mcdodik.springai.db.entity.prompt.ChunkingPromptTemplate
import mcdodik.springai.db.entity.prompt.PromptGenerationRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for generating prompt templates based on user input.
 * Provides an endpoint to generate a [ChunkingPromptTemplate] using domain name and user description.
 */
@RestController
@RequestMapping("/api/prompt")
class PromptGeneratorController(
    private val service: PromptGenerationService
) {

    /**
     * Endpoint to generate a prompt template.
     *
     * @param request A [PromptGenerationRequest] containing the domain name and user description.
     * @return A generated [ChunkingPromptTemplate].
     */
    @PostMapping("/generate")
    fun generatePrompt(@RequestBody request: PromptGenerationRequest): ChunkingPromptTemplate {
        return service.generatePrompt(request.domainName, request.userDescription)
    }

    /** Endpoint to find prompt templates by topic (semantic + fallback). */
    @PostMapping("/find-by-topic")
    fun findByTopic(@RequestBody req: FindByTopicRequest): FindByTopicResponse {
        val results = service.findByTopic(req.topic, req.k, req.minSim)
        return FindByTopicResponse(
            items = results.map {
                FindByTopicResponse.Item(
                    id = it.template.id,
                    domainName = it.template.domainName,
                    topic = it.template.topic,
                    score = it.score,
                    preview = it.template.generatedPrompt.take(SYMBOL_TO_VIEW)
                )
            }

        )
    }

    companion object {
        const val SYMBOL_TO_VIEW = 200
    }
}
