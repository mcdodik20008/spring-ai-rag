package mcdodik.springai.infrastructure.youtube.service.impl

import mcdodik.springai.infrastructure.youtube.model.TranscriptEntry
import mcdodik.springai.infrastructure.youtube.service.api.YouTubeTranscriptApi
import mcdodik.springai.infrastructure.youtube.service.api.YoutubeSubtitleService
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets

@Service
class YoutubeSubtitleServiceImpl(
    private val yt: YouTubeTranscriptApi
) : YoutubeSubtitleService {

    override suspend fun getFile(videoId: String): MultipartFile {
        val id = normalizeVideoId(videoId)

        val entries: List<TranscriptEntry> = try {
            yt.fetch(id, languages = listOf("ru", "en"))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Автосубтитры (ru/en) не найдены для видео $id", e)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Не удалось получить субтитры с YouTube", e)
        }

        val text = entries.joinToString(separator = " ") { it.text }
            .replace(Regex("\\s+"), " ")
            .trim()

        if (text.isBlank()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Пустые субтитры для видео $id")
        }

        val filename = "$id.txt"
        val bytes = text.toByteArray(StandardCharsets.UTF_8)

        return MockMultipartFile(
            "file",
            filename,
            "text/plain; charset=utf-8",
            bytes
        )
    }

    private fun normalizeVideoId(input: String): String {
        val regex = Regex("""(?:v=|youtu\.be/)([a-zA-Z0-9_-]{11})""")
        return regex.find(input)?.groupValues?.get(1) ?: input
    }
}
