package mcdodik.springai.webcrawler.source

import mcdodik.springai.webcrawler.model.CrawledData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * Источник данных с веб-страниц
 * todo: Этот класс либо сделать абстрактным с реализациями под каждый сайт, либо реализовать список опрашиваемых
 */
@Component
class WebPageSource : WebSource {
    override val name: String = "webpage"

    override fun crawl(): Flux<CrawledData> {
        // TODO: Реализовать парсинг веб-страниц.

        /*
         * Для реализации этой функции нужно:
         *
         * 1. Добавить зависимости в build.gradle.kts:
         *    implementation("org.jsoup:jsoup:1.15.3")
         *
         * 2. Настроить список URL в application.properties:
         *    webpage.urls=https://example1.com,https://example2.com
         *
         * 3. Инжектить конфигурацию:
         *    @Value("\${webpage.urls}")
         *    private lateinit var urls: String
         *
         * 4. Реализовать логику:
         *    - Получить список URL для мониторинга
         *    - Для каждого URL загрузить страницу
         *    - Извлечь заголовок и контент
         *    - Преобразовать в CrawledData
         *
         * Примерная реализация:
         *
         * val urlList = urls.split(",")
         * return Flux.fromIterable(urlList)
         *     .flatMap { url ->
         *         val document = Jsoup.connect(url).get()
         *         val title = document.title()
         *         val content = document.body().text()
         *
         *         Flux.just(
         *             CrawledData(
         *                 source = name,
         *                 url = url,
         *                 title = title,
         *                 content = content,
         *                 timestamp = LocalDateTime.now()
         *             )
         *         )
         *     }
         */

        return Flux.empty()
    }

    // Вспомогательный метод для извлечения данных из веб-страницы
    private fun extractDataFromUrl(url: String): CrawledData? {
        /*
         * try {
         *     val document = Jsoup.connect(url).get()
         *     val title = document.title()
         *     val content = document.select("p").text() // Извлекаем только текст из параграфов
         *
         *     return CrawledData(
         *         source = name,
         *         url = url,
         *         title = title,
         *         content = content,
         *         timestamp = LocalDateTime.now()
         *     )
         * } catch (e: Exception) {
         *     // Обработка ошибок
         *     return null
         * }
         */
        return null
    }
}
