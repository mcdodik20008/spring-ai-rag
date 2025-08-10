package mcdodik.springai.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {
        val http =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected {
                    it.addHandlerLast(ReadTimeoutHandler(30, TimeUnit.SECONDS))
                    it.addHandlerLast(WriteTimeoutHandler(30, TimeUnit.SECONDS))
                }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(http))
            .filter(logRequest())
            .filter(logResponse())
            .build()
    }

    private fun logRequest() =
        ExchangeFilterFunction.ofRequestProcessor { req ->
            // при желании втащи логгер
            // logger.debug("HTTP {} {}", req.method(), req.url())
            reactor.core.publisher.Mono.just(req)
        }

    private fun logResponse() =
        ExchangeFilterFunction.ofResponseProcessor { res ->
            // logger.debug("HTTP -> status {}", res.statusCode())
            reactor.core.publisher.Mono.just(res)
        }
}
