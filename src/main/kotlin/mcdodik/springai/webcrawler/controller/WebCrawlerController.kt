package mcdodik.springai.webcrawler.controller

import mcdodik.springai.webcrawler.model.CrawledData
import mcdodik.springai.webcrawler.service.WebCrawlerService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/webcrawler")
class WebCrawlerController(
    private val webCrawlerService: WebCrawlerService,
) {
    @GetMapping("/crawl", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun crawlAllSources(): Flux<CrawledData> {
        return webCrawlerService.crawlAllSources()
    }

    @GetMapping("/crawl-to-rag", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun crawlAndIngestAllSources(): Flux<String> {
        return webCrawlerService.crawlAndIngestAllSources()
    }

    @GetMapping("/crawl/{sourceName}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun crawlSource(
        @PathVariable sourceName: String,
    ): Flux<CrawledData> {
        return webCrawlerService.crawlSource(sourceName)
    }

    @GetMapping("/crawl-to-rag/{sourceName}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun crawlAndIngestSource(
        @PathVariable sourceName: String,
    ): Flux<String> {
        return webCrawlerService.crawlAndIngestSource(sourceName)
    }
}
