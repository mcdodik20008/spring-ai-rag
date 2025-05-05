package mcdodik.springai.utils.document

import mcdodik.springai.utils.book.PdfCleanRequest
import mcdodik.springai.utils.book.PdfCleaner
import mcdodik.springai.utils.reader.CodeAwareTikaReader
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class PdfDocumentWorker(
    private val textSplitter: TokenTextSplitter,
    private val pdfCleaner: PdfCleaner
) : DocumentWorker {
    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "application/pdf" || file.originalFilename?.endsWith(".pdf") == true

    override fun process(file: MultipartFile): List<Document> {
        val cleanedStream = pdfCleaner.cleanPdf(file.inputStream, PdfCleanRequest())
        val resource = InputStreamResource(cleanedStream)
        val pageReader = CodeAwareTikaReader(resource)
        val chunks = textSplitter.apply(pageReader.read())
        return textSplitter.apply(chunks)
    }
}
