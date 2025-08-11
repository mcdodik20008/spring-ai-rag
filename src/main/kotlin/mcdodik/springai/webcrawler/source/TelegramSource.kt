package mcdodik.springai.webcrawler.source

import mcdodik.springai.webcrawler.model.CrawledData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Component
class TelegramSource : WebSource {
    override val name: String = "telegram"

    override fun crawl(): Flux<CrawledData> {
        // Это заглушка для Telegram источника
        // В реальной реализации здесь будет код для подключения к Telegram API
        return Flux.empty()
        /*
        return Flux.fromIterable(
            listOf(
                CrawledData(
                    source = name,
                    url = "https://t.me/channel/message1",
                    title = "Telegram Message 1",
                    content = "Content of telegram message 1",
                    timestamp = LocalDateTime.now()
                )
            )
        )
        */
    }
}
