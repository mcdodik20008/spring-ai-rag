package mcdodik.springai.controller

import mcdodik.springai.model.AskRequest
import mcdodik.springai.model.AskResponse
import mcdodik.springai.service.RagService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ask")
class AskController(private val rag: RagService) {

    @PostMapping
    fun ask(@RequestBody req: AskRequest): AskResponse =
        AskResponse(rag.ask(req.question))

}