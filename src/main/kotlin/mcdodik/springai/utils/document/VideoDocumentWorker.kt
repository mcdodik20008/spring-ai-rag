package mcdodik.springai.utils.document

import mcdodik.springai.utils.transcriprion.TranscriptionService
import org.springframework.ai.document.Document
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class VideoDocumentWorker(
    private val transcriptionService: TranscriptionService
) : DocumentWorker {

    override fun supports(file: MultipartFile): Boolean =
        file.contentType?.startsWith("video/") == true

    override fun process(file: MultipartFile): List<Document> {
        val transcript = transcriptionService.transcribe(file)
        return listOf(Document(transcript, mapOf("source" to file.originalFilename)))
    }
}
