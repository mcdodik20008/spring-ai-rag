package mcdodik.springai.rag.application

import mcdodik.springai.rag.api.ContextBuilder
import mcdodik.springai.rag.model.Metadata
import org.springframework.ai.document.Document

class MarkdownContextBuilder : ContextBuilder {
    override fun build(docs: List<Document>, maxChars: Int): String {
        val sb = StringBuilder()
        for (d in docs) {
            val file = Metadata.fileName(d) ?: "unknown"
            val idx = Metadata.chunkIndex(d)?.toString() ?: "?"
            val chunk = "### file:$file chunk:$idx\n${d.text.orEmpty()}\n\n"
            if (sb.length + chunk.length > maxChars) break
            sb.append(chunk)
        }
        return sb.toString()
    }
}