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
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import kotlinx.coroutines.reactor.awaitSingle

@Component
class YouTubeTranscriptApiImpl(
    @Qualifier("youtubeWebClient")
    private val webClient: WebClient,
) : YouTubeTranscriptApi,
    Loggable {
    private val mapper = jacksonObjectMapper()

    override suspend fun fetch(
        videoId: String,
        languages: List<String>,
    ): List<TranscriptEntry> {
        val tracks = list(videoId)
        logger.debug("Found tracks: {}", tracks.joinToString { "${it.languageCode}:${it.kind ?: "-"}" })

        val track =
            tracks
                .sortedWith(
                    compareBy<CaptionTrack> { it.languageCode !in languages }
                        .thenBy { it.kind != "asr" },
                ).firstOrNull()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Автосубтитры (ru/en) не найдены для видео $videoId")

        val raw = track.baseUrl
        val unescaped =
            org.apache.commons.text.StringEscapeUtils.unescapeHtml4(
                org.apache.commons.text.StringEscapeUtils
                    .unescapeHtml4(raw),
            )

        val uri =
            UriComponentsBuilder
                .fromUriString(unescaped)
                .apply {
                    val built = build(true)
                    val hasFmt = built.queryParams.containsKey("fmt")
                    if (!hasFmt) queryParam("fmt", "vtt")
                }.build(true) // не трогать уже закодированное
                .toUri()

        logger.debug("Fetching captions from: {}", uri)

        val vtt =
            webClient
                .get()
                .uri(uri)
                .retrieve()
                .onStatus({ !it.is2xxSuccessful }) { resp ->
                    resp.bodyToMono(String::class.java).flatMap { body ->
                        logger.error("Error ${resp.statusCode()} from YouTube, body:\n$body")
                        Mono.error(RuntimeException("YouTube error: ${resp.statusCode()}"))
                    }
                }.bodyToMono(String::class.java)
                .doOnNext { body -> logger.debug("VTT response body:\n$body") }
                .awaitSingle()

        logger.debug(
            """
            response fro youtube: $vtt
            """.trimIndent(),
        )

        return vttToPlainText(vtt)
    }

    private suspend fun list(videoId: String): List<CaptionTrack> {
        logger.debug("▶️ Start list(videoId={})", videoId)

        val html =
            webClient
                .get()
                .uri("/watch?v={v}&hl=en", videoId)
                .retrieve()
                .bodyToMono(String::class.java)
                .awaitSingle()

        logger.debug("📄 watch page: length={}", html.length)
        // Быстрые евристики — полезно понять, это нормальная страница или consent/ошибка
        logger.debug("📄 contains ytInitialPlayerResponse: {}", html.contains("ytInitialPlayerResponse"))
        logger.debug("📄 contains ytcfg.set(: {}", html.contains("ytcfg.set("))
        logger.debug("📄 contains consent.youtube.com: {}", html.contains("consent.youtube.com"))
        logger.debug("📄 head snippet: '{}'", html.take(200).replace("\n", " "))

        val playerJson =
            extractPlayerResponseJson(html) ?: run {
                logger.error("❌ ytInitialPlayerResponse not found after parsing for {}", videoId)
                throw ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "YouTube watch page parsed without ytInitialPlayerResponse",
                )
            }

        logger.debug(
            "✅ playerJson length={}, head='{}'",
            playerJson.length,
            playerJson.take(200).replace("\n", " "),
        )

        val root = mapper.readTree(playerJson)
        val tracksNode =
            root
                .path("captions")
                .path("playerCaptionsTracklistRenderer")
                .path("captionTracks")

        logger.debug(
            "🔍 captions isArray={}, size={}",
            tracksNode.isArray,
            if (tracksNode.isArray) tracksNode.size() else -1,
        )

        if (!tracksNode.isArray || tracksNode.isEmpty) {
            logger.warn("⚠️ No captions for {}", videoId)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No captions available for video $videoId")
        }

        val result = mapper.readValue<List<CaptionTrack>>(tracksNode.toString())
        logger.debug("🎬 tracks: {}", result.joinToString { "${it.languageCode}:${it.kind ?: "-"}" })
        return result
    }

    private fun extractPlayerResponseJson(html: String): String? {
        // A) ytInitialPlayerResponse = { ... }
        run {
            val re = Regex("""(?:var\s+)?ytInitialPlayerResponse\s*=\s*""")
            val m = re.find(html)
            logger.debug("🔎 A) ytInitialPlayerResponse '=' found: {}", m != null)
            if (m != null) {
                val json = extractJsonObjectFrom(html, m.range.last + 1)
                logger.debug("🔎 A) parsed object present: {}", json != null)
                if (json != null) return json
            }
        }

        // B) window["ytInitialPlayerResponse"] = { ... }
        run {
            val re = Regex("""window\[\s*["']ytInitialPlayerResponse["']\s*]\s*=\s*""")
            val m = re.find(html)
            logger.debug("🔎 B) window[...] '=' found: {}", m != null)
            if (m != null) {
                val json = extractJsonObjectFrom(html, m.range.last + 1)
                logger.debug("🔎 B) parsed object present: {}", json != null)
                if (json != null) return json
            }
        }

        // C/D) все ytcfg.set({...});
        run {
            val re = Regex("""ytcfg\.set\(\s*(\{.*?})\s*\);""", RegexOption.DOT_MATCHES_ALL)
            val all = re.findAll(html).toList()
            logger.debug("🔎 C) ytcfg.set matches: {}", all.size)

            for ((idx, match) in all.withIndex()) {
                val cfgStr = match.groupValues[1]
                val cfgLen = cfgStr.length
                logger.debug("   C[{}] cfg length={}", idx, cfgLen)
                val cfg =
                    try {
                        mapper.readTree(cfgStr)
                    } catch (e: Exception) {
                        logger.warn("   C[{}] cfg parse error: {}", idx, e.message)
                        continue
                    }

                fun tryNode(name: String): String? {
                    val node = cfg.get(name) ?: return null
                    return if (node.isObject) {
                        logger.debug("   C[{}] {} isObject -> OK", idx, name)
                        node.toString()
                    } else if (node.isTextual) {
                        logger.debug("   C[{}] {} isTextual -> unescape", idx, name)
                        val unescaped = mapper.readValue("\"${node.asText()}\"", String::class.java)
                        unescaped.replace("\\u0026", "&")
                    } else {
                        null
                    }
                }

                tryNode("PLAYER_RESPONSE")?.let { return it }
                tryNode("PLAYER_RESPONSE_ARMOR")?.let { return it }

                // Фолбэк: ищем captionTracks внутри cfg
                val captionsNode = cfg.findValue("captions")
                val tracks = captionsNode?.path("playerCaptionsTracklistRenderer")?.path("captionTracks")
                val size = if (tracks != null && tracks.isArray) tracks.size() else -1
                logger.debug("   C[{}] captions->tracks size={}", idx, size)
                if (tracks != null && tracks.isArray && tracks.size() > 0) {
                    val root = mapper.createObjectNode()
                    val capRoot = mapper.createObjectNode()
                    capRoot.set<com.fasterxml.jackson.databind.JsonNode>(
                        "playerCaptionsTracklistRenderer",
                        mapper.readTree("""{"captionTracks":$tracks}"""),
                    )
                    root.set<com.fasterxml.jackson.databind.JsonNode>("captions", capRoot)
                    return root.toString()
                }
            }
        }

        logger.debug("🔚 extractPlayerResponseJson: nothing matched")
        return null
    }

    private fun extractJsonObjectFrom(
        html: String,
        startIdx: Int,
    ): String? {
        var i = startIdx
        while (i < html.length && html[i].isWhitespace()) i++
        if (i >= html.length || html[i] != '{') return null

        var depth = 0
        var j = i
        var inStr = false
        var quote = '\u0000'
        var esc = false

        while (j < html.length) {
            val c = html[j]
            if (inStr) {
                if (esc) {
                    esc = false
                } else {
                    if (c == '\\') {
                        esc = true
                    } else if (c == quote) {
                        inStr = false
                    }
                }
            } else {
                when (c) {
                    '"', '\'' -> {
                        inStr = true
                        quote = c
                    }
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return html.substring(i, j + 1)
                    }
                }
            }
            j++
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
                                text = text.toString().trim(),
                            ),
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
                    start = start,
                    duration = (end!! - start),
                    text = text.toString().trim(),
                ),
            )
        }
        return result
    }

    private fun hmsToSeconds(
        h: String,
        m: String,
        s: String,
        ms: String,
    ): Double = h.toInt() * 3600 + m.toInt() * 60 + s.toInt() + ms.toInt() / 1000.0
}
