package mcdodik.springai.utils.book.parser

import mcdodik.springai.utils.book.model.DocumentFragment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream

@Component
class DocxParser : BookParser {
    override fun supports(file: File) = file.extension.equals("docx", ignoreCase = true)

    override fun parse(file: File): List<DocumentFragment> {
        val fragments = mutableListOf<DocumentFragment>()
        FileInputStream(file).use { fis ->
            val doc = XWPFDocument(fis)
            var paraIndex = 1
            doc.paragraphs.forEach { p ->
                fragments += DocumentFragment(pageNumber = null, header = null, text = p.text)
                paraIndex++
            }
        }
        return fragments
    }
}
