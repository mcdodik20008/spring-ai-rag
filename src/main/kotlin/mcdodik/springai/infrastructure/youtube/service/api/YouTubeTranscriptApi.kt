package mcdodik.springai.infrastructure.youtube.service.api

import mcdodik.springai.infrastructure.youtube.model.TranscriptEntry

interface YouTubeTranscriptApi {

    suspend fun fetch(
        videoId: String,
        languages: List<String> = listOf("en")
    ): List<TranscriptEntry>

}
