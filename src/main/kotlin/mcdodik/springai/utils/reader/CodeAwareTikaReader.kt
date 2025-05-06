package mcdodik.springai.utils.reader

import mcdodik.springai.rag.formatting.RagChunkDto
import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentReader
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.core.io.Resource

class CodeAwareTikaReader : DocumentReader {

    val delegate: TikaDocumentReader

    constructor(resource: Resource) {
        this.delegate = TikaDocumentReader(resource)
    }

    override fun get(): List<Document> {
        val allDocuments = delegate.get()

        val buffer = StringBuilder()
        val metadata = mutableMapOf<String, Any>()

        allDocuments.forEach { doc ->
            buffer.appendLine(doc.text)
            metadata.putAll(doc.metadata)
        }

        val logicalBlocks = splitByCodeBlocks(buffer.toString())

        val chunks = logicalBlocks.mapIndexed { i, block ->
            @Suppress("UNCHECKED_CAST")
            val typedMetadata = (metadata + mapOf(
                "type" to block.type,
                "chunk_index" to i
            )) as Map<String, Object>

            DocumentChunk(
                content = block.content,
                metadata = typedMetadata
            )
        }

        return mapChunksToDocuments(chunks)
    }

    private fun mapChunksToDocuments(chunks: List<DocumentChunk>): List<Document> {
        return chunks.map { chunk ->
            Document(
                "chunk-${chunk.metadata["chunk_index"]}",
                chunk.content,
                chunk.metadata
            )
        }
    }

    private fun splitByCodeBlocks(content: String): List<RagChunkDto> {
        val result = mutableListOf<RagChunkDto>()
        val buffer = StringBuilder()
        var insideCode = false

        for (line in content.lines()) {
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                if (insideCode) {
                    buffer.appendLine(line)
                    result += RagChunkDto(buffer.toString().trim(), "code")
                    buffer.clear()
                    insideCode = false
                } else {
                    if (buffer.isNotBlank()) {
                        result += RagChunkDto(buffer.toString().trim(), "text")
                        buffer.clear()
                    }
                    buffer.appendLine(line)
                    insideCode = true
                }
            } else {
                buffer.appendLine(line)
            }
        }

        if (buffer.isNotBlank()) {
            val type = if (insideCode) "code" else "text"
            result += RagChunkDto(buffer.toString().trim(), type)
        }

        return result
    }

}
