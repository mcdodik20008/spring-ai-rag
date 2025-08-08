package mcdodik.springai.utils.documentworker

import mcdodik.springai.api.controller.responses.CleanRequestParams
import mcdodik.springai.api.controller.responses.PdfCleanRequest
import mcdodik.springai.extension.fetchInfoFromFile
import mcdodik.springai.utils.cleaner.DocumentCleaner
import mcdodik.springai.utils.reader.CodeAwareTikaReaderFactory
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

    override fun process(file: MultipartFile, params: CleanRequestParams): List<Document> {
        val cleanedStream = cleaner.doIt(file.inputStream, params as PdfCleanRequest)
        val resource = InputStreamResource(cleanedStream)
        val pageReader = readerFactory.create(resource)
        val read = pageReader.read()
        read.forEachIndexed { n, doc -> doc.fetchInfoFromFile(n, file) }
        return textSplitter.apply(read)
    }
}
