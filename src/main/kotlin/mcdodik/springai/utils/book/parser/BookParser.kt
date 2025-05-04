package mcdodik.springai.utils.book.parser

import mcdodik.springai.utils.book.model.DocumentFragment
import org.springframework.stereotype.Component
import java.io.File

@Component
interface BookParser {
    /** Возвращает упорядоченный список фрагментов */
    fun parse(file: File): List<DocumentFragment>

    /** Проверяет, подходит ли формат под этот парсер */
    fun supports(file: File): Boolean
}
