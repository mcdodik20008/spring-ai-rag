package mcdodik.springai.infrastructure.youtube.service.api

import org.springframework.web.multipart.MultipartFile

interface YoutubeSubtitleService {
    suspend fun getFile(videoId: String): MultipartFile
}
