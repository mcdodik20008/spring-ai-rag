package mcdodik.springai.utils.documentworker

import mcdodik.springai.controller.model.PdfCleanRequest
import mcdodik.springai.utils.cleaner.DocumentCleaner
import mcdodik.springai.utils.reader.CodeAwareTikaReader
import mcdodik.springai.utils.reader.CodeAwareTikaReaderFactory
import org.mozilla.universalchardet.ReaderFactory
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class PdfDocumentWorker(
    private val textSplitter: TokenTextSplitter,
    private val cleaner: DocumentCleaner,
    private val readerFactory: CodeAwareTikaReaderFactory
) : DocumentWorker {
    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "application/pdf" || file.originalFilename?.endsWith(".pdf") == true

    override fun process(file: MultipartFile, params: PdfCleanRequest): List<Document> {
        val cleanedStream = cleaner.doIt(file.inputStream, params)
        val resource = InputStreamResource(cleanedStream)
        val pageReader = readerFactory.create(resource)
        val read = pageReader.read()
        return textSplitter.apply(read)
    }
}
