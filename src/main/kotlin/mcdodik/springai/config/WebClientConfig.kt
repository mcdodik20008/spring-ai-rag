package mcdodik.springai.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.random.Random

@Configuration
class WebClientConfig {
    @Bean
    @Primary
    fun webClient(): WebClient {
        val provider =
            ConnectionProvider.builder("openrouter-pool")
                .maxConnections(100) // подбери под нагрузку
                .pendingAcquireMaxCount(1_000)
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .evictInBackground(Duration.ofSeconds(30))
                .lifo() // лучше для бурстов
                .build()

        val http =
            HttpClient.create(provider)
                .compress(true) // gzip
                .keepAlive(true)
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected {
                    it.addHandlerLast(ReadTimeoutHandler(30, TimeUnit.SECONDS))
                    it.addHandlerLast(WriteTimeoutHandler(30, TimeUnit.SECONDS))
                }
        // .wiretap(true) // включай при отладке

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(http))
            // .baseUrl("https://openrouter.ai") // если используешь только OpenRouter; иначе оставь без baseUrl
            .codecs { cfg ->
                cfg.defaultCodecs().maxInMemorySize(4 * 1024 * 1024) // 4MB buffer
            }
            .filter(defaultHeadersFilter())
            .filter(logRequest())
            .filter(logResponse())
            .filter(retryOn429Filter(maxRetries = 3))
            .build()
    }

// --- вспомогательные фильтры ---

    private fun retryOn429Filter(
        maxRetries: Int = 3,
        defaultBackoff: Duration = Duration.ofSeconds(1),
        maxBackoff: Duration = Duration.ofSeconds(15),
    ): ExchangeFilterFunction {
        return ExchangeFilterFunction { request, next ->

            fun attempt(tryNo: Int): Mono<ClientResponse> =
                next.exchange(request).flatMap { resp ->
                    val sc = resp.statusCode()
                    val isRetryable = (sc == HttpStatus.TOO_MANY_REQUESTS || sc == HttpStatus.SERVICE_UNAVAILABLE)

                    if (isRetryable) {
                        // важно: освободить соединение (прочитать/дренировать тело) перед ретраем
                        resp.bodyToMono(Void::class.java)
                            .onErrorResume { Mono.empty() }
                            .then(
                                if (tryNo >= maxRetries) {
                                    Mono.error(map429ToException(resp, tryNo))
                                } else {
                                    val delay = computeRetryDelay(resp, defaultBackoff, maxBackoff, tryNo)
                                    Mono.delay(delay).flatMap { attempt(tryNo + 1) }
                                },
                            )
                    } else {
                        Mono.just(resp)
                    }
                }

            attempt(1)
        }
    }

    private fun map429ToException(
        resp: ClientResponse,
        tryNo: Int,
    ): Throwable {
        val retryAfter = resp.headers().header("Retry-After").firstOrNull()
        val status = resp.statusCode()
        return org.springframework.web.reactive.function.client.WebClientResponseException.create(
            status.value(),
            "Upstream rate limited after $tryNo tries ($status), Retry-After=$retryAfter",
            resp.headers().asHttpHeaders(),
            ByteArray(0),
            null,
        )
    }

    private fun computeRetryDelay(
        resp: ClientResponse,
        defaultBackoff: Duration,
        maxBackoff: Duration,
        tryNo: Int,
    ): Duration {
        // Retry-After может быть секундами или HTTP-date
        val ra = resp.headers().header("Retry-After").firstOrNull()
        val base =
            when {
                ra == null -> defaultBackoff.multipliedBy(1L shl (tryNo - 1))
                ra.all { it.isDigit() } ->
                    defaultBackoff.multipliedBy(max(1, ra.toLong())).coerceAtMost(maxBackoff)

                else -> {
                    runCatching { ZonedDateTime.parse(ra, DateTimeFormatter.RFC_1123_DATE_TIME) }.getOrNull()
                        ?.let { date ->
                            val delta = Duration.between(ZonedDateTime.now(date.zone), date)
                            if (!delta.isNegative) delta else defaultBackoff
                        } ?: defaultBackoff
                }
            }
        // джиттер 0..300мс, чтобы не биться в такт
        val jitterMs = Random.nextLong(0, 300)
        return base.plusMillis(jitterMs).coerceAtMost(maxBackoff)
    }

    private fun defaultHeadersFilter(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor { req: ClientRequest ->
            val mutated =
                ClientRequest.from(req)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                    .header(HttpHeaders.USER_AGENT, "spring-ai-rag/1.0 (+webclient)")
                    .build()
            Mono.just(mutated)
        }

    // Заглушки, оставляем твои реализационные фильтры
    private fun logRequest(): ExchangeFilterFunction = ExchangeFilterFunction.ofRequestProcessor { Mono.just(it) }

    private fun logResponse(): ExchangeFilterFunction = ExchangeFilterFunction.ofResponseProcessor { Mono.just(it) }
}
