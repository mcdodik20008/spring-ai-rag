package mcdodik.springai.utils.transcriprion

import org.springframework.web.multipart.MultipartFile

interface TranscriptionService {
    fun transcribe(file: MultipartFile): String
}
