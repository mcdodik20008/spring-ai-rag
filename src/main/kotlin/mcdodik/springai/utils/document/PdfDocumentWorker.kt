package mcdodik.springai.utils.document

import mcdodik.springai.utils.book.parser.BookParserService
import org.springframework.ai.document.Document
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class PdfDocumentWorker(
    private val textSplitter: TokenTextSplitter,
    private val parser: BookParserService
) : DocumentWorker {
    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "application/pdf" || file.originalFilename?.endsWith(".pdf") == true

    override fun process(file: MultipartFile): List<Document> {
        parser.parse(file) // Argument type mismatch: actual type is 'org. springframework. web. multipart. MultipartFile', but 'java. io. File' was expected.
        val resource = InputStreamResource(file.inputStream)
        val pageReader = TikaDocumentReader(resource)
        val chunks = textSplitter.apply(pageReader.read())
        return textSplitter.apply(chunks)
    }
}
