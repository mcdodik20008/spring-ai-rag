package mcdodik.springai.db.entity.rag

import org.springframework.ai.document.Document
import java.time.LocalDateTime
import java.util.UUID

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
    val chunkIndex: Int,
    val fileName: String,
    val extension: String,
    val hash: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(it: Document): RagChunkEntity =
            RagChunkEntity(
                id = UUID.randomUUID(),
                content = it.text ?: "empty",
                embedding = it.metadata[MetadataKey.EMBEDDING.key] as List<Float>,
                type = it.metadata[MetadataKey.TYPE.key]?.toString() ?: "empty",
                source = it.metadata[MetadataKey.SOURCE.key]?.toString() ?: "empty",
                chunkIndex = it.metadata[MetadataKey.CHUNK_INDEX.key] as Int,
                fileName = it.metadata[MetadataKey.FILE_NAME.key].toString(),
                extension = it.metadata[MetadataKey.EXTENSION.key].toString(),
                hash = it.metadata[MetadataKey.HASH.key].toString(),
                createdAt = LocalDateTime.now(),
            )
    }
}
