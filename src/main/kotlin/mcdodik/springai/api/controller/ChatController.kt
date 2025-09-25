package mcdodik.springai.api.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping
class ChatController {
    @GetMapping("/chat")
    fun chatPage(): String = "chat"
}
