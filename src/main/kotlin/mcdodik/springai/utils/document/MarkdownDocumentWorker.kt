package mcdodik.springai.utils.document

import mcdodik.springai.extension.visitChildren
import mcdodik.springai.utils.cleaner.PdfCleanRequest
import org.commonmark.node.*
import org.commonmark.parser.Parser
import org.springframework.ai.document.Document
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class MarkdownDocumentWorker : DocumentWorker {

    private val parser = Parser.builder().build()

    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "text/markdown" || file.originalFilename?.endsWith(".md") == true

    override fun process(file: MultipartFile, params: PdfCleanRequest): List<Document> {
        val markdown = file.inputStream.bufferedReader().use { it.readText() }
        val documentNode = parser.parse(markdown)

        val visitor = MarkdownVisitor(file.originalFilename ?: "unknown")
        documentNode.accept(visitor)

        return visitor.documents
    }

    private class MarkdownVisitor(private val source: String) : AbstractVisitor() {

        val documents = mutableListOf<Document>()
        private val contentBuffer = StringBuilder()
        private var currentType = "text"
        private var currentHeadingLevel = 0

        override fun visit(heading: Heading) {
            flushBuffer()
            currentType = "heading"
            currentHeadingLevel = heading.level
            heading.visitChildren(this)
            flushBuffer(mapOf("headingLevel" to currentHeadingLevel))
        }

        override fun visit(fencedCodeBlock: FencedCodeBlock) {
            flushBuffer()
            documents += Document(
                fencedCodeBlock.literal,
                mapOf(
                    "type" to "code",
                    "language" to (fencedCodeBlock.info ?: "unknown"),
                    "source" to source
                )
            )
        }

        override fun visit(bulletList: BulletList) {
            flushBuffer()
            currentType = "list"
            bulletList.visitChildren(this)
            flushBuffer()
        }

        override fun visit(listItem: ListItem) {
            contentBuffer.append("- ")
            listItem.visitChildren(this)
            contentBuffer.append("\n")
        }

        override fun visit(paragraph: Paragraph) {
            currentType = "paragraph"
            paragraph.visitChildren(this)
            contentBuffer.append("\n")
        }

        override fun visit(text: Text) {
            contentBuffer.append(text.literal)
        }

        override fun visit(thematicBreak: ThematicBreak) {
            flushBuffer()
        }

        private fun flushBuffer(extraMetadata: Map<String, Any> = emptyMap()) {
            if (contentBuffer.isNotBlank()) {
                documents += Document(
                    contentBuffer.toString().trim(),
                    mutableMapOf<String, Any>(
                        "type" to currentType,
                        "source" to source
                    ).apply { putAll(extraMetadata) }
                )
                contentBuffer.clear()
            }
            currentType = "text"
            currentHeadingLevel = 0
        }
    }
}
