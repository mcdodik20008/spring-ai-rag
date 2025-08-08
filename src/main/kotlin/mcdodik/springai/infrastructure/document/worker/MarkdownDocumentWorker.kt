package mcdodik.springai.infrastructure.document.worker

import mcdodik.springai.api.dto.CleanRequestParams
import mcdodik.springai.extensions.fetchInfoFromFile
import mcdodik.springai.infrastructure.document.reader.CodeAwareTikaReaderFactory
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.core.io.InputStreamResource
import org.springframework.web.multipart.MultipartFile

class MarkdownDocumentWorker(
    private val textSplitter: TokenTextSplitter,
    private val readerFactory: CodeAwareTikaReaderFactory,
) : DocumentWorker {

    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "text/markdown" || file.originalFilename?.endsWith(".md") == true

    override fun process(file: MultipartFile, params: CleanRequestParams): List<Document> {
        val resource = InputStreamResource(file.inputStream)
        val reader = readerFactory.create(resource)
        val rawText = reader.read()
        rawText.forEachIndexed { n, doc -> doc.fetchInfoFromFile(n, file) }

        return textSplitter.apply(rawText)
    }
}
