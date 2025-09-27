package mcdodik.springai.api.restcontroller

import mcdodik.springai.api.dto.user.ChatMessageResponse
import mcdodik.springai.api.dto.user.ConversationResponse
import mcdodik.springai.api.service.ChatMessageService
import mcdodik.springai.api.service.ConversationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/conversations")
class ConversationController(
    private val conversationService: ConversationService,
    private val chatMessageService: ChatMessageService,
) {
    @GetMapping
    fun getConversationsForUser(
        @RequestParam userId: Long,
    ): ResponseEntity<List<ConversationResponse>> {
        val conversations = conversationService.findConversationsByUserId(userId)
        return ResponseEntity.ok(conversations)
    }

    @GetMapping("/{conversationId}/messages")
    fun getMessagesInConversation(
        @PathVariable conversationId: Long,
    ): ResponseEntity<List<ChatMessageResponse>> {
        val messages = chatMessageService.findMessagesByConversationId(conversationId)
        return ResponseEntity.ok(messages)
    }
}
