package mcdodik.springai.utils.document

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class DocumentWorkerFactory(
    private val workers: List<DocumentWorker>
) {
    fun getWorker(file: MultipartFile): DocumentWorker =
        workers.firstOrNull { it.supports(file) }
            ?: throw IllegalArgumentException("Unsupported file type: ${file.contentType}")
}
