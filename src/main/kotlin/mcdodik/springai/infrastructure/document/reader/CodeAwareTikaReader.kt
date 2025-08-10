package mcdodik.springai.infrastructure.document.reader

import mcdodik.springai.rag.model.RagChunkDto
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

        return logicalBlocks.mapIndexed { i, block ->
            val typedMetadata =
                (
                    metadata +
                        mapOf(
                            "type" to block.type,
                            "chunk_index" to i,
                        )
                ) as Map<String, Any?>

            Document(
                "chunk-$i",
                block.content,
                typedMetadata,
            )
        }
    }

    private fun splitByCodeBlocks(content: String): List<RagChunkDto> {
        val result = mutableListOf<RagChunkDto>()
        val buffer = StringBuilder()
        var insideCode = false

        for (line in content.lines()) {
            insideCode = isInsideCode(line, insideCode, buffer, result)
        }

        if (buffer.isNotBlank()) {
            val type = if (insideCode) "code" else "text"
            result += RagChunkDto(buffer.toString().trim(), type)
        }

        return result
    }

    private fun isInsideCode(
        line: String,
        insideCode: Boolean,
        buffer: StringBuilder,
        result: MutableList<RagChunkDto>,
    ): Boolean {
        var insideCode1 = insideCode
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (insideCode1) {
                buffer.appendLine(line)
                result += RagChunkDto(buffer.toString().trim(), "code")
                buffer.clear()
                insideCode1 = false
            } else {
                if (buffer.isNotBlank()) {
                    result += RagChunkDto(buffer.toString().trim(), "text")
                    buffer.clear()
                }
                buffer.appendLine(line)
                insideCode1 = true
            }
        } else {
            buffer.appendLine(line)
        }
        return insideCode1
    }
}
