package mcdodik.springai.utils.document

import mcdodik.springai.service.cleanPdfText
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.ai.document.Document
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class PdfDocumentWorker : DocumentWorker {
    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "application/pdf" || file.originalFilename?.endsWith(".pdf") == true

    override fun process(file: MultipartFile): List<Document> {
        val text = Loader.loadPDF(file.inputStream.readAllBytes()).use { pdf ->
            PDFTextStripper()
                .apply { sortByPosition = true }
                .getText(pdf)
                .cleanPdfText()
        }

        return listOf(Document(text, mapOf("source" to file.originalFilename)))
    }
}
