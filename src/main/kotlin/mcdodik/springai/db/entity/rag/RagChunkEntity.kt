package mcdodik.springai.db.entity.rag

import java.time.LocalDateTime
import java.util.UUID
import org.springframework.ai.document.Document

/**
 * Data class representing a document chunk stored in the system.
 *
 * This class is used to store individual text chunks of a document, along with their vector embeddings and metadata.
 */
@Suppress("UNCHECKED_CAST")
data class RagChunkEntity(
    val id: UUID = UUID.randomUUID(),
    val content: String,
    val embedding: List<Float>,
    val type: String,
    val source: String,
    val chunkIndex: Integer,
    val fileName: String,
    val extension: String,
    val hash: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(it: Document): RagChunkEntity {
            println("БАЗУХА БРАТ! ${it.metadata[DocumentMetadataKey.EMBEDDING.key].toString()}")
            return RagChunkEntity(
                id = UUID.randomUUID(),
                content = it.text ?: "empty",
                embedding = it.metadata[DocumentMetadataKey.EMBEDDING.key] as List<Float>,
                type = it.metadata[DocumentMetadataKey.TYPE.key]?.toString() ?: "empty",
                source = it.metadata[DocumentMetadataKey.SOURCE.key]?.toString() ?: "empty",
                chunkIndex = it.metadata[DocumentMetadataKey.CHUNK_INDEX.key] as Integer,
                fileName = it.metadata[DocumentMetadataKey.FILE_NAME.key].toString(),
                extension = it.metadata[DocumentMetadataKey.EXTENSION.key].toString(),
                hash = it.metadata[DocumentMetadataKey.HASH.key].toString(),
                createdAt = LocalDateTime.now()
            )
        }
    }
}
