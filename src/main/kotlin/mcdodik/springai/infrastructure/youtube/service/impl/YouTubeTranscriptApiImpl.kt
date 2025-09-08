package mcdodik.springai.infrastructure.youtube.service.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mcdodik.springai.config.Loggable
import mcdodik.springai.infrastructure.youtube.model.CaptionTrack
import mcdodik.springai.infrastructure.youtube.model.TranscriptEntry
import mcdodik.springai.infrastructure.youtube.service.api.YouTubeTranscriptApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import kotlinx.coroutines.reactor.awaitSingle

@Component
class YouTubeTranscriptApiImpl(
    @Qualifier("youtubeWebClient")
    private val webClient: WebClient
) : YouTubeTranscriptApi, Loggable {

    private val mapper = jacksonObjectMapper()

    override suspend fun fetch(
        videoId: String,
        languages: List<String>
    ): List<TranscriptEntry> {
        val tracks = list(videoId)

        val track = tracks.firstOrNull { it.languageCode in languages }
            ?: throw IllegalArgumentException("No transcript found for $languages")

        val vtt = webClient.get()
            .uri(track.baseUrl + if (track.baseUrl.contains("fmt=")) "" else "&fmt=vtt")
            .retrieve()
            .onStatus({ status -> !status.is2xxSuccessful }) { resp ->
                resp.bodyToMono(String::class.java).flatMap { body ->
                    logger.error("Error ${resp.statusCode()} from YouTube, body:\n$body")
                    Mono.error(RuntimeException("YouTube error: ${resp.statusCode()}"))
                }
            }
            .bodyToMono(String::class.java)
            .doOnNext { body -> logger.debug("VTT response body:\n$body") }
            .awaitSingle()

        logger.debug(
            """
            response fro youtube: $vtt
        """.trimIndent()
        )

        return vttToPlainText(vtt)
    }

    private suspend fun list(videoId: String): List<CaptionTrack> {
        val html = webClient.get()
            .uri("/watch?v={v}&hl=en", videoId)
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()

        val playerJson = extractPlayerResponseJson(html)
            ?: throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "YouTube watch page parsed without ytInitialPlayerResponse"
            )

        val root = mapper.readTree(playerJson)
        val tracksNode = root.path("captions")
            .path("playerCaptionsTracklistRenderer")
            .path("captionTracks")

        if (!tracksNode.isArray || tracksNode.isEmpty) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No captions available for video $videoId"
            )
        }

        return mapper.readValue(tracksNode.toString())
    }

    private fun extractPlayerResponseJson(html: String): String? {
        // Вариант 1: ytInitialPlayerResponse = { ... };
        val idx = html.indexOf("ytInitialPlayerResponse")
        if (idx >= 0) {
            // ищем '=' после ключа
            val eq = html.indexOf('=', idx)
            if (eq > 0) {
                // пропускаем пробелы
                var i = eq + 1
                while (i < html.length && html[i].isWhitespace()) i++
                if (i < html.length && html[i] == '{') {
                    val start = i
                    var depth = 0
                    var j = i
                    while (j < html.length) {
                        val c = html[j]
                        if (c == '{') depth++
                        if (c == '}') {
                            depth--
                            if (depth == 0) {
                                // ожидаем ';' после JSON
                                val json = html.substring(start, j + 1)
                                return json
                            }
                        }
                        j++
                    }
                }
            }
        }

        // Вариант 2: "PLAYER_RESPONSE":"{...}" (строка с экранированием)
        val re2 = Regex(""""PLAYER_RESPONSE"\s*:\s*"(\{.*?})"""", RegexOption.DOT_MATCHES_ALL)
        re2.find(html)?.let {
            val escaped = it.groupValues[1]
            // YouTube кодирует \u0026 как &, а также экранирует слэши
            return escaped
                .replace("""\u0026""", "&")
                .replace("""\\u0026""", "&")
                .replace("""\\""", """\""")
        }
        return null
    }

    private fun vttToPlainText(vtt: String): List<TranscriptEntry> {
        val result = mutableListOf<TranscriptEntry>()
        val lines = vtt.lines()
        var start: Double? = null
        var end: Double? = null
        val text = StringBuilder()

        val timeRe = Regex("""(\d{2}):(\d{2}):(\d{2})\.(\d{3}) --> (\d{2}):(\d{2}):(\d{2})\.(\d{3})""")

        for (line in lines) {
            val m = timeRe.find(line)
            when {
                m != null -> {
                    // новый блок → если был старый, сохранить
                    if (start != null && text.isNotBlank()) {
                        result.add(
                            TranscriptEntry(
                                start = start,
                                duration = (end!! - start),
                                text = text.toString().trim()
                            )
                        )
                        text.clear()
                    }
                    start = hmsToSeconds(m.groupValues[1], m.groupValues[2], m.groupValues[3], m.groupValues[4])
                    end = hmsToSeconds(m.groupValues[5], m.groupValues[6], m.groupValues[7], m.groupValues[8])
                }

                line.isNotBlank() && !line.startsWith("WEBVTT") -> {
                    text.append(line).append(" ")
                }
            }
        }
        if (start != null && text.isNotBlank()) {
            result.add(
                TranscriptEntry(
                    start = start!!,
                    duration = (end!! - start!!),
                    text = text.toString().trim()
                )
            )
        }
        return result
    }

    private fun hmsToSeconds(h: String, m: String, s: String, ms: String): Double =
        h.toInt() * 3600 + m.toInt() * 60 + s.toInt() + ms.toInt() / 1000.0
}
