package mcdodik.springai.infrastructure.youtube.config

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

@Component
class WebClientBodyLoggingFilter(
    private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger("webclient.body"),
) : ExchangeFilterFunction {
    override fun filter(
        request: ClientRequest,
        next: ExchangeFunction,
    ): Mono<ClientResponse> {
        log.debug("➡️ {} {}", request.method(), request.url())
        request.headers().forEach { (k, v) -> log.debug("   {}: {}", k, v) }

        return next.exchange(request).flatMap { resp ->
            val ct = resp.headers().contentType().orElse(null)
            val isText =
                ct?.let { t ->
                    TEXT_MEDIA.any { prefix -> t.toString().startsWith(prefix) }
                } ?: false

            if (!isText) {
                log.debug("⬅️ {} {} (non-text body)", resp.statusCode(), request.url())
                return@flatMap Mono.just(resp)
            }

            // читаем body как String, логируем и возвращаем «новый» ClientResponse с тем же body
            resp.bodyToMono(String::class.java).defaultIfEmpty("").flatMap { body ->
                val preview = if (body.length > 2000) body.substring(0, 2000) + "…[truncated]" else body
                log.debug("⬅️ {} {} body:\n{}", resp.statusCode(), request.url(), preview)

                val rebuilt =
                    ClientResponse
                        .create(resp.statusCode())
                        .headers { h -> resp.headers().asHttpHeaders().forEach { (k, v) -> h.addAll(k, v) } }
                        .cookies { c -> resp.cookies().forEach { (k, v) -> c.addAll(k, v) } }
                        .body(body)
                        .build()

                Mono.just(rebuilt)
            }
        }
    }

    companion object {
        private val TEXT_MEDIA = setOf("text/", "application/json", "application/xml")
    }
}
