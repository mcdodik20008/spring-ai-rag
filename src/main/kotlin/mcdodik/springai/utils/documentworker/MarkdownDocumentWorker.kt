package mcdodik.springai.utils.documentworker

import mcdodik.springai.controller.model.CleanRequestParams
import mcdodik.springai.utils.reader.CodeAwareTikaReaderFactory
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class MarkdownDocumentWorker(
    private val textSplitter: TokenTextSplitter,
    private val readerFactory: CodeAwareTikaReaderFactory
) : DocumentWorker {

    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "text/markdown" || file.originalFilename?.endsWith(".md") == true

    override fun process(file: MultipartFile, params: CleanRequestParams): List<Document> {
        val resource = InputStreamResource(file.inputStream)
        val reader = readerFactory.create(resource)
        val rawText = reader.read()


        return textSplitter.apply(rawText)
    }
}
