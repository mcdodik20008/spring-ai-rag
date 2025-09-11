package mcdodik.springai.webcrawler.config

import mcdodik.springai.apiai.v1.serivces.IngestionService
import mcdodik.springai.webcrawler.service.RagIngestionService
import mcdodik.springai.webcrawler.service.WebCrawlerService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["mcdodik.springai.webcrawler"])
class WebCrawlerConfig {
    @Bean
    fun ragIngestionService(ingestionService: IngestionService): RagIngestionService {
        return RagIngestionService(ingestionService)
    }

    @Bean
    fun webCrawlerService(
        webSources: List<mcdodik.springai.webcrawler.source.WebSource>,
        ragIngestionService: RagIngestionService,
    ): WebCrawlerService {
        return WebCrawlerService(webSources, ragIngestionService)
    }
}
