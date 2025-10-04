package mcdodik.springai.api.restcontroller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.WebSession

@RestController
class SessionProbeController {
    @GetMapping("/probe")
    suspend fun probe(session: WebSession): Map<String, Any> {
        val n = (session.getAttribute<Int>("n") ?: 0) + 1
        session.attributes["n"] = n
        return mapOf("sessionId" to session.id, "counter" to n)
    }
}
