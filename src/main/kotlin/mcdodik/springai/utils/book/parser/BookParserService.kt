package mcdodik.springai.utils.book.parser

import mcdodik.springai.utils.book.model.DocumentFragment
import org.springframework.stereotype.Component
import java.io.File

@Component
class BookParserService(private val parsers: List<BookParser>) {

    fun parse(file: File): List<DocumentFragment> {
        val parser = parsers.find { it.supports(file) }
            ?: error("Нет поддерживаемого парсера для ${file.extension}")
        return parser.parse(file)
    }
}
