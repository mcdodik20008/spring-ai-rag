package mcdodik.springai.utils.book.parser

import mcdodik.springai.utils.book.model.DocumentFragment
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.sax.BodyContentHandler
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream

@Component
class PdfParser : BookParser {
    override fun supports(file: File) = file.extension.equals("pdf", ignoreCase = true)

    override fun parse(file: File): List<DocumentFragment> {
        val handler = BodyContentHandler(-1)
        val metadata = Metadata()
        val context = ParseContext().apply {
            set(Parser::class.java, PDFParser())
        }

        FileInputStream(file).use { stream ->
            PDFParser().parse(stream, handler, metadata, context)
        }
        // Здесь можно парсить handler.toString(), разбивая по номерам страниц
        // Для упрощения — весь текст одним фрагментом
        return listOf(DocumentFragment(pageNumber = null, header = null, text = handler.toString()))
    }
}