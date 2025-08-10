package mcdodik.springai.apiai.v1.config

import mcdodik.springai.config.Loggable
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Configuration
class TraceLoggingConfig : Loggable {
    companion object {
        const val TRACE_HEADER = "X-Trace-Id"
        const val TRACE_KEY = "trace_id"
    }

    @Bean
    fun traceLoggingFilter(): WebFilter =
        WebFilter { exchange: ServerWebExchange, chain: WebFilterChain ->
            val start = Instant.now()

            val incomingTrace = exchange.request.headers.getFirst(TRACE_HEADER)
            val traceId = incomingTrace ?: UUID.randomUUID().toString()

            // пробрасываем в ответ
            exchange.response.headers.add(TRACE_HEADER, traceId)

            // лёгкий request log
            val req = exchange.request
            logWithTrace(traceId) {
                val q = if (req.uri.rawQuery != null) "?${req.uri.rawQuery}" else ""
                logger.info("→ {} {}{}", req.method, req.path.value(), q)
            }

            chain.filter(exchange)
                .doOnSuccess {
                    // response log
                    val dur = Duration.between(start, Instant.now()).toMillis()
                    val status = exchange.response.statusCode ?: 200
                    logWithTrace(traceId) {
                        logger.info("← {} {} ({} ms, status={})", req.method, req.path.value(), dur, status)
                    }
                }
                .doOnError { ex ->
                    val dur = Duration.between(start, Instant.now()).toMillis()
                    logWithTrace(traceId) {
                        logger.warn("× {} {} failed after {} ms: {}", req.method, req.path.value(), dur, ex.toString())
                    }
                }
                // положим trace в Reactor Context (на случай, если дальше кто-то его поднимет)
                .contextWrite { ctx -> ctx.put(TRACE_KEY, traceId) }
        }

    private inline fun <T> logWithTrace(
        traceId: String,
        block: () -> T,
    ): T {
        MDC.put(TRACE_KEY, traceId)
        return try {
            block()
        } finally {
            MDC.remove(TRACE_KEY)
        }
    }
}
