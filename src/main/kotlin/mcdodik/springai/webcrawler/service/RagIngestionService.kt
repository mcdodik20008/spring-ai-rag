package mcdodik.springai.webcrawler.service

import mcdodik.springai.api.dto.CustomMultipartFile
import mcdodik.springai.apiai.v1.serivces.IngestionService
import mcdodik.springai.webcrawler.model.CrawledData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class RagIngestionService(
    private val ingestionService: IngestionService
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RagIngestionService::class.java)
        private const val DEFAULT_KB_ID = "default"
    }

    fun ingestCrawledData(data: CrawledData): Mono<String> {
        logger.info("Ingesting crawled data: ${data.title} from ${data.source}")
        
        // Преобразуем CrawledData в MultipartFile
        val multipartFile = createMultipartFile(data)
        
        // Используем существующий IngestionService для загрузки
        return ingestionService.upload(
            kbId = DEFAULT_KB_ID,
            file = multipartFile,
            llmChunking = true,
            chunkingPromptId = null
        ).map { documentDto ->
            logger.info("Successfully ingested document: ${documentDto.id}")
            documentDto.id
        }.onErrorResume { error ->
            logger.error("Error ingesting crawled data: ${error.message}", error)
            Mono.just("error")
        }
    }

    private fun createMultipartFile(data: CrawledData): MultipartFile {
        // Создаем текстовый контент из данных
        val contentBuilder = StringBuilder()
        contentBuilder.append("# ${data.title}\n\n")
        contentBuilder.append("Source: ${data.source}\n")
        contentBuilder.append("URL: ${data.url}\n")
        contentBuilder.append("Crawled at: ${data.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}\n\n")
        contentBuilder.append(data.content)
        
        // Добавляем метаданные
        if (data.metadata.isNotEmpty()) {
            contentBuilder.append("\n\n## Metadata\n")
            data.metadata.forEach { (key, value) ->
                contentBuilder.append("- $key: $value\n")
            }
        }
        
        val content = contentBuilder.toString()
        val filename = "${data.source}_${data.id}.md"
        
        return CustomMultipartFile(
            name = "webcrawl",
            originalFilename = filename,
            contentType = "text/markdown",
            content = content.toByteArray()
        )
    }
}
