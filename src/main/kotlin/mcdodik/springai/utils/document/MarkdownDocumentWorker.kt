package mcdodik.springai.utils.document

import org.springframework.ai.document.Document
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class MarkdownDocumentWorker : DocumentWorker {
    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "text/markdown" || file.originalFilename?.endsWith(".md") == true

    override fun process(file: MultipartFile): List<Document> {
        val content = file.inputStream.bufferedReader().use { it.readText() }
        return listOf(Document(content, mapOf("source" to file.originalFilename)))
    }
}
