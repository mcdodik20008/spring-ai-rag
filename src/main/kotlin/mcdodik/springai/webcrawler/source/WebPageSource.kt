package mcdodik.springai.webcrawler.source

import mcdodik.springai.webcrawler.model.CrawledData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Component
class WebPageSource : WebSource {
    override val name: String = "webpage"

    override fun crawl(): Flux<CrawledData> {
        // Это заглушка для веб-страниц источника
        // В реальной реализации здесь будет код для парсинга веб-страниц
        return Flux.empty()
        /*
        return Flux.fromIterable(
            listOf(
                CrawledData(
                    source = name,
                    url = "https://example.com/page1",
                    title = "Web Page 1",
                    content = "Content of web page 1",
                    timestamp = LocalDateTime.now()
                )
            )
        )
        */
    }
}
