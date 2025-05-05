package mcdodik.springai.utils.document

import mcdodik.springai.utils.book.PdfCleanRequest
import org.springframework.ai.document.Document
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class DocumentWorkerFactory(
    private val workers: List<DocumentWorker>
) {
    fun getWorker(file: MultipartFile): DocumentWorker =
        workers.firstOrNull { it.supports(file) }
            ?: throw IllegalArgumentException("Unsupported file type: ${file.contentType}")

    fun process(file: MultipartFile, params: PdfCleanRequest, ): List<Document> {
        val worker = getWorker(file)
        return worker.process(file, params)
    }
}
