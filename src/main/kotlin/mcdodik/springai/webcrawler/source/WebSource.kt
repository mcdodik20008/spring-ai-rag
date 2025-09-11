package mcdodik.springai.webcrawler.source

import mcdodik.springai.webcrawler.model.CrawledData
import reactor.core.publisher.Flux

interface WebSource {
    val name: String

    fun crawl(): Flux<CrawledData>
}
