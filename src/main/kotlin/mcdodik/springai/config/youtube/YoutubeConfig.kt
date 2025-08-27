package mcdodik.springai.config.youtube

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class YoutubeConfig {
    @Bean
    @Qualifier("youtubeWebClient")
    fun youtubeWebClient(loggingFilter: WebClientBodyLoggingFilter): WebClient {
        val httpClient = HttpClient.create()
            .wiretap(true)
            .responseTimeout(Duration.ofSeconds(20))

        val strategies = ExchangeStrategies.builder()
            .codecs { it.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) } // 10 MB
            .build()

        return WebClient.builder()
            .baseUrl("https://www.youtube.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .filter(loggingFilter)
            .defaultHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            )
            .defaultHeader("Accept-Language", "en-US,en;q=0.9,ru;q=0.8")
            .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            // EU consent bypass (важно для Латвии)
            .defaultHeader("Cookie", "CONSENT=YES+cb.20210328-17-p0.en+F+678")
            .build()
    }
}
