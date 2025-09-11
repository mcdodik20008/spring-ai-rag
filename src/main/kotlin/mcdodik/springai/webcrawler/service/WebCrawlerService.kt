package mcdodik.springai.webcrawler.service

import mcdodik.springai.webcrawler.model.CrawledData
import mcdodik.springai.webcrawler.source.WebSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class WebCrawlerService(
    private val webSources: List<WebSource>,
    private val ragIngestionService: RagIngestionService,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WebCrawlerService::class.java)
    }

    fun crawlAllSources(): Flux<CrawledData> {
        logger.info("Starting crawl of all web sources")
        return Flux.fromIterable(webSources)
            .flatMap { source ->
                logger.info("Crawling source: ${source.name}")
                source.crawl()
                    .doOnError { error ->
                        logger.error("Error crawling source ${source.name}: ${error.message}", error)
                    }
                    .onErrorResume { Mono.empty() }
            }
    }

    fun crawlAndIngestAllSources(): Flux<String> {
        logger.info("Starting crawl and ingest of all web sources")
        return crawlAllSources()
            .flatMap { data ->
                ragIngestionService.ingestCrawledData(data)
                    .onErrorResume { error ->
                        logger.error("Error ingesting data: ${error.message}", error)
                        Mono.just("error")
                    }
            }
    }

    fun crawlSource(sourceName: String): Flux<CrawledData> {
        val source = webSources.find { it.name.equals(sourceName, ignoreCase = true) }
        return if (source != null) {
            logger.info("Crawling source: ${source.name}")
            source.crawl()
                .doOnError { error ->
                    logger.error("Error crawling source ${source.name}: ${error.message}", error)
                }
                .onErrorResume { Mono.empty() }
        } else {
            logger.warn("Source not found: $sourceName")
            Flux.empty()
        }
    }

    fun crawlAndIngestSource(sourceName: String): Flux<String> {
        logger.info("Starting crawl and ingest of source: $sourceName")
        return crawlSource(sourceName)
            .flatMap { data ->
                ragIngestionService.ingestCrawledData(data)
                    .onErrorResume { error ->
                        logger.error("Error ingesting data: ${error.message}", error)
                        Mono.just("error")
                    }
            }
    }
}
