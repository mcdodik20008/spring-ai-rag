package mcdodik.springai.rag.service.impl

import mcdodik.springai.rag.model.Metadata
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.service.api.ContextBuilder

class MarkdownContextBuilder : ContextBuilder {
    override fun build(
        docs: List<RetrievedDoc>,
        maxChars: Int,
    ): String {
        val sb = StringBuilder()
        for (d in docs) {
            val file = Metadata.fileName(d) ?: "unknown"
            val idx = Metadata.chunkIndex(d)?.toString() ?: "?"
            val chunk = "### file:$file chunk:$idx\n${d.content}\n\n"
            if (sb.length + chunk.length > maxChars) break
            sb.append(chunk)
        }
        return sb.toString()
    }
}
