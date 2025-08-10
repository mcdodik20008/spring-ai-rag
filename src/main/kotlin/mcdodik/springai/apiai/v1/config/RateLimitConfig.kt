package mcdodik.springai.apiai.v1.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Configuration
class RateLimitConfig(
    @Value("\${app.ratelimit.enabled:true}") private val enabled: Boolean,
    @Value("\${app.ratelimit.capacity:10}") private val capacity: Long,
    @Value("\${app.ratelimit.refill-per-min:10}") private val perMinute: Long,
) {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    @Bean
    fun ingestionRateLimitFilter(): WebFilter =
        WebFilter { exchange: ServerWebExchange, chain: WebFilterChain ->
            if (!enabled || !isLimitedPath(exchange.request)) return@WebFilter chain.filter(exchange)

            val key = rateKey(exchange.request)
            val bucket = buckets.computeIfAbsent(key) { newBucket() }

            val probe = bucket.tryConsumeAndReturnRemaining(1)
            if (!probe.isConsumed) {
                exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                exchange.response.headers.add("X-RateLimit-Remaining", "0")
                exchange.response.headers.add("Retry-After", "60")
                return@WebFilter exchange.response.setComplete()
            }

            exchange.response.headers.add("X-RateLimit-Remaining", probe.remainingTokens.toString())
            chain.filter(exchange)
        }

    private fun newBucket(): Bucket {
        val limit =
            Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(perMinute, Duration.ofMinutes(1))
                .build()
        return Bucket.builder().addLimit(limit).build()
    }

    private fun isLimitedPath(req: ServerHttpRequest): Boolean {
        val p = req.path.value()
        // режем только "тяжёлые" точки
        return (p.startsWith("/v1/ingestions") && req.method.matches("POST") == true) ||
            (p == "/v1/documents" && req.method.matches("POST") == true)
    }

    private fun rateKey(req: ServerHttpRequest): String {
        // при наличии авторизации — лучше по subject/tenant
        val auth = req.headers.getFirst("Authorization") ?: ""
        return when {
            auth.startsWith("Bearer ") -> auth.hashCode().toString()
            else -> (req.headers.getFirst("X-Forwarded-For") ?: req.remoteAddress?.address?.hostAddress ?: "anon")
        }
    }
}
