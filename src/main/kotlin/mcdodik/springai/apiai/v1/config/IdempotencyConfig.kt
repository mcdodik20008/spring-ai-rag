package mcdodik.springai.apiai.v1.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Configuration
class IdempotencyConfig(
    @Value("\${app.idempotency.enabled:true}") private val enabled: Boolean,
    @Value("\${app.idempotency.ttl-seconds:3600}") private val ttlSec: Long,
) {
    private val store = ConcurrentHashMap<String, Entry>()



    @Bean
    fun idempotencyFilter(): WebFilter =
        WebFilter { exchange, chain ->
            if (!enabled || exchange.request.method != HttpMethod.POST) return@WebFilter chain.filter(exchange)

            val key = exchange.request.headers.getFirst("Idempotency-Key") ?: return@WebFilter chain.filter(exchange)
            cleanup()

            val cached = store[key]
            if (cached != null && Instant.now().epochSecond - cached.createdAt <= ttlSec) {
                val resp = exchange.response
                resp.statusCode = HttpStatus.valueOf(cached.status)
                val buffer = resp.bufferFactory().wrap(cached.body)
                return@WebFilter resp.writeWith(Mono.just(buffer))
            }

            val decoratedExchange = BufferingExchangeDecorator(exchange)
            return@WebFilter chain.filter(decoratedExchange).then(
                Mono.fromRunnable {
                    val respDec = decoratedExchange.responseDecorator()
                    val body = respDec.bodyBytes()
                    store[key] = Entry(respDec.statusCode().value(), body)
                },
            )
        }

    private fun cleanup() {
        val now = Instant.now().epochSecond
        store.entries.removeIf { now - it.value.createdAt > ttlSec }
    }
}
