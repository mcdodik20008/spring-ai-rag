package mcdodik.springai.infrastructure.cleaner

import mcdodik.springai.api.dto.PdfCleanRequest
import mcdodik.springai.extensions.hasGlyph
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

@Component
class PdfCleaner : DocumentCleaner {
    override fun doIt(
        input: InputStream,
        params: PdfCleanRequest,
    ): InputStream {
        val cleaned = ByteArrayOutputStream()

        Loader.loadPDF(input.readAllBytes()).use { document ->
            val stripper = PDFTextStripper()
            stripper.startPage = 1 + params.skipPages
            stripper.endPage = document.numberOfPages - params.throwPagesFromEnd

            val pages = mutableListOf<List<String>>()

            for (pageNum in stripper.startPage..stripper.endPage) {
                stripper.startPage = pageNum
                stripper.endPage = pageNum
                val text = stripper.getText(document)
                val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
                pages.add(lines)
            }

            val headers = findRepeatingLines(pages, params.headerFooterLines, params.repeatThreshold, fromTop = true)
            val footers = findRepeatingLines(pages, params.headerFooterLines, params.repeatThreshold, fromTop = false)

            val cleanedPages =
                pages.map { lines ->
                    lines.filterNot { it in headers || it in footers }
                }

            storePdf(cleanedPages, cleaned)
        }

        return ByteArrayInputStream(cleaned.toByteArray())
    }

    private fun storePdf(
        cleanedPages: List<List<String>>,
        cleaned: ByteArrayOutputStream,
    ) {
        PDDocument().use { newDoc ->
            val fontStream =
                javaClass.getResourceAsStream("/fonts/DejaVuSans.ttf")
                    ?: error("Шрифт DejaVuSans.ttf не найден в resources/fonts/")
            val font = PDType0Font.load(newDoc, fontStream, true) // важно: embed = true

            cleanedPages.forEach { lines ->
                val page = PDPage(PDRectangle.LETTER)
                newDoc.addPage(page)

                contentStream(newDoc, page, font, lines)
            }

            // (опционально) сохранить локально для отладки
            newDoc.save(File("C:\\Users\\mcdod\\OneDrive\\Рабочий стол\\test.pdf"))
            newDoc.save(cleaned)
        }
    }

    private fun contentStream(
        newDoc: PDDocument,
        page: PDPage,
        font: PDType0Font,
        lines: List<String>,
    ) {
        PDPageContentStream(newDoc, page).use { content ->
            content.beginText()
            content.setFont(font, FONT_SIZE)
            content.newLineAtOffset(CONTENT_NEW_LINE_AT_OFFSET_FROM, CONTENT_NEW_LINE_AT_OFFSET_TO)

            lines.forEach { line ->
                val safeLine = line.filter { font.hasGlyph(it) }
                if (safeLine.isNotBlank()) {
                    content.showText(safeLine)
                    content.newLineAtOffset(TEXT_NEW_LINE_AT_OFFSET_FROM, TEXT_NEW_LINE_AT_OFFSET_TO)
                }
            }

            content.endText()
        }
    }

    companion object {
        const val FONT_SIZE = 12f
        const val CONTENT_NEW_LINE_AT_OFFSET_FROM = 50f
        const val CONTENT_NEW_LINE_AT_OFFSET_TO = 750f
        const val TEXT_NEW_LINE_AT_OFFSET_FROM = 0f
        const val TEXT_NEW_LINE_AT_OFFSET_TO = -15f
    }

    private fun findRepeatingLines(
        pages: List<List<String>>,
        linesToCheck: Int,
        repeatThreshold: Double,
        fromTop: Boolean,
    ): Set<String> {
        val lineFrequency = mutableMapOf<String, Int>()
        pages.forEach { lines ->
            val relevant = if (fromTop) lines.take(linesToCheck) else lines.takeLast(linesToCheck)
            for (line in relevant) {
                lineFrequency[line] = lineFrequency.getOrDefault(line, 0) + 1
            }
        }

        val minCount = (pages.size * repeatThreshold).toInt()
        return lineFrequency.filter { it.value >= minCount }.keys
    }
}
