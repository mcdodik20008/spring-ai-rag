package mcdodik.springai.utils.book

import mcdodik.springai.extension.hasGlyph
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
class PdfCleaner {

    fun cleanPdf(input: InputStream, params: PdfCleanRequest): InputStream {
        val cleaned = ByteArrayOutputStream()

        Loader.loadPDF(input.readAllBytes()).use { document ->
            val stripper = PDFTextStripper()
            stripper.startPage = 1 + params.skipPages
            stripper.endPage = document.numberOfPages - params.skipPages

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

            val cleanedPages = pages.map { lines ->
                lines.filterNot { it in headers || it in footers }
            }

            PDDocument().use { newDoc ->
                val fontStream = javaClass.getResourceAsStream("/fonts/DejaVuSans.ttf")
                    ?: error("Шрифт DejaVuSans.ttf не найден в resources/fonts/")
                val font = PDType0Font.load(newDoc, fontStream, true) // важно: embed = true


                cleanedPages.forEach { lines ->
                    val page = PDPage(PDRectangle.LETTER)
                    newDoc.addPage(page)

                    PDPageContentStream(newDoc, page).use { content ->
                        content.beginText()
                        content.setFont(font, 12f)
                        content.newLineAtOffset(50f, 750f)

                        lines.forEach { line ->
                            val safeLine = line.filter { font.hasGlyph(it) }
                            if (safeLine.isNotBlank()) {
                                content.showText(safeLine)
                                content.newLineAtOffset(0f, -15f)
                            }
                        }

                        content.endText()
                    }
                }

                // (опционально) сохранить локально для отладки
                newDoc.save(File("C:\\Users\\mcdod\\OneDrive\\Рабочий стол\\test.pdf"))
                newDoc.save(cleaned)
            }
        }

        return ByteArrayInputStream(cleaned.toByteArray())
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