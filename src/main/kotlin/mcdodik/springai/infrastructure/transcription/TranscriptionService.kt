package mcdodik.springai.infrastructure.transcription

import org.springframework.web.multipart.MultipartFile

interface TranscriptionService {
    fun transcribe(file: MultipartFile): String
}
