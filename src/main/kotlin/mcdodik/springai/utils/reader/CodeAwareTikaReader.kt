package mcdodik.springai.utils.reader

import org.springframework.ai.document.Document
import org.springframework.ai.document.DocumentReader
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.core.io.Resource

class CodeAwareTikaReader : DocumentReader {

    val delegate : TikaDocumentReader

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
}
