package mcdodik.springai.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
import org.springframework.ai.reader.markdown.MarkdownDocumentReader
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class RagService(
    private val chat: ChatClient,
    private val vectorStore: VectorStore,
) {

    private val splitter: TokenTextSplitter = TokenTextSplitter.builder().withChunkSize(300).build()

    fun ask(question: String): String? =
        chat.prompt().user(question).call().content()

    fun ingest(markdown: String) {
        val resource = object : ByteArrayResource(markdown.toByteArray()) {
            override fun getFilename(): String = "inline.md"
        }
        val documentReader = MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig())
        vectorStore.write(
            splitter.apply(documentReader.read())
        )
    }

    fun ingestPdf(file: MultipartFile) {
        val text = Loader.loadPDF(file.inputStream.readAllBytes()).use { pdf ->
            PDFTextStripper()
                .apply { sortByPosition = true }
                .getText(pdf)
                .cleanPdfText()
        }

        val docs = listOf(
            Document(text, mapOf("source" to (file.originalFilename ?: "upload.pdf")))
        )

        vectorStore.write(splitter.apply(docs))
    }
}



/**
 * Упрощённая нормализация текста, извлечённого из PDF.
 *
 *  1. Переводит CRLF → LF
 *  2. Склеивает перенос‑с‑дефисом (`hyphen-\nated` → `hyphenated`)
 *  3. Удаляет хвостовые/начальные пробелы в строках
 *  4. Сжимает 3+ пустых строк до одной
 *  5. Заменяет не‑разрывный пробел (U+00A0) на обычный
 *  6. Сводит подряд идущие пробелы к одному
 */
fun String.cleanPdfText(): String =
    this
        .replace("\r\n", "\n")                          // CRLF → LF
        .replace(Regex("-\\s*\\n\\s*"), "")             // de‑hyphen
        .replace(Regex("\\u00A0"), " ")                 // nbsp → space
        .replace(Regex("(?m)^\\s+|\\s+\$"), "")         // trim each line
        .replace(Regex("\\s{2,}"), " ")                 // collapse spaces
        .replace(Regex("\\n{3,}"), "\n\n")              // collapse blank lines


