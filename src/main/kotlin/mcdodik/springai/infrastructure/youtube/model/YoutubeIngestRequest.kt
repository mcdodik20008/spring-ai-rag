package mcdodik.springai.infrastructure.youtube.model

import io.swagger.v3.oas.annotations.media.Schema

data class YoutubeIngestRequest(
    @Schema(
        description = "ID видео YouTube или полная ссылка",
        example = "https://www.youtube.com/watch?v=nHaFozCs4dI",
    )
    val videoId: String,
) {
    fun normalizedVideoId(): String {
        val regex = Regex("""(?:v=|youtu\.be/)([a-zA-Z0-9_-]{11})""")
        return regex.find(videoId)?.groupValues?.get(1) ?: videoId
    }
}
