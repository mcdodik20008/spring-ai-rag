package mcdodik.springai.webcrawler.source

import mcdodik.springai.webcrawler.model.CrawledData
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * Источник данных с ТГ чатов
 * todo: Этот класс либо сделать абстрактным с реализациями под каждый чат, либо реализовать список опрашиваемых
 */
@Component
class TelegramSource : WebSource {
    override val name: String = "telegram"

    override fun crawl(): Flux<CrawledData> {
        // TODO: Реализовать подключение к Telegram API

        /*
         * Для реализации этой функции нужно:
         * DONE
         * 1. Добавить зависимости в build.gradle.kts:
         *    implementation("org.telegram:telegrambots:6.1.0")
         *    или
         *    implementation("com.github.pengrad:java-telegram-bot-api:6.0.0")
         *
         * 2. Настроить конфигурацию в application.properties:
         *    telegram.bot.token=your_bot_token
         *    telegram.channels=channel1,channel2,channel3
         *
         * 3. Инжектить конфигурацию:
         *    @Value("\${telegram.token}")
         *    private lateinit var botToken: String
         *
         * 4. Реализовать логику:
         *    - Подключиться к Telegram Bot API
         *    - Получить список каналов для мониторинга
         *    - Получить последние сообщения из каналов
         *    - Преобразовать сообщения в CrawledData
         *
         * Примерная реализация:
         *
         * return telegramClient.getMessages(channels)
         *     .map { message ->
         *         CrawledData(
         *             source = name,
         *             url = "https://t.me/${message.channel}/${message.id}",
         *             title = "Сообщение из ${message.channel}",
         *             content = message.text,
         *             timestamp = message.timestamp,
         *             metadata = mapOf(
         *                 "messageId" to message.id,
         *                 "channel" to message.channel,
         *                 "views" to message.views
         *             )
         *         )
         *     }
         */

        return Flux.empty()
    }
}
