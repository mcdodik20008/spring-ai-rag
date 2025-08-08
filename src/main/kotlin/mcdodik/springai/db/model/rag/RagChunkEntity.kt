package mcdodik.springai.db.model.rag

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
    /**
     * Unique identifier for the document chunk. Automatically generated using UUID.randomUUID() if not provided.
     */
    val id: UUID = UUID.randomUUID(),

    /**
     * The content (text) of the document chunk.
     */
    val content: String,

    /**
     * Vector embedding of the document chunk's content.
     */
    val embedding: List<Float>,

    /**
     * Type of the document chunk (e.g., "paragraph", "code", etc.).
     */
    val type: String,

    /**
     * Source identifier or name where this chunk originated from.
     */
    val source: String,

    /**
     * Index of the chunk within the original document.
     */
    val chunkIndex: Integer,

    /**
     * Name of the original file associated with the document.
     */
    val fileName: String,

    /**
     * File extension of the original file (e.g., "txt", "md").
     */
    val extension: String,

    /**
     * SHA-256 hash of the original file's content, used to identify duplicates or unchanged files.
     */
    val hash: String,

    /**
     * Timestamp when the chunk was created or added to the system.
     */
    val createdAt: LocalDateTime
) {
    /**
     * Companion object providing factory methods for creating instances of [RagChunkEntity].
     */
    companion object {
        /**
         * Creates a new [RagChunkEntity] instance from a [Document] object.
         * Extracts metadata and content from the given document to populate the entity fields.
         *
         * @param it The [Document] object containing the content and metadata.
         * @return A new [RagChunkEntity] object with the extracted information.
         */
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
