package mcdodik.springai.rag.db

import java.time.LocalDateTime
import java.util.UUID
import org.springframework.ai.document.Document


@Suppress("UNCHECKED_CAST")
data class RagChunkEntity(
    val id: UUID = UUID.randomUUID(),
    val content: String,
    val embedding: List<Float>?,
    val type: String?,
    val source: String?,
    val chunkIndex: Integer,
    val fileName: String,
    val extension: String,
    val hash: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun from(it: Document): RagChunkEntity {
            println("БАЗУХА БРАТ! ${it.metadata["embedding"].toString()}")
            return RagChunkEntity(
                id = UUID.randomUUID(),
                content = it.text ?: "empty",
                embedding = it.metadata["embedding"] as List<Float>?,
                type = it.metadata["type"]?.toString(),
                source = it.metadata["source"]?.toString(),
                chunkIndex = it.metadata["chunk_index"] as Integer,
                fileName = it.metadata["file_name"].toString(),
                extension = it.metadata["extension"].toString(),
                hash = it.metadata["hash"].toString(),
                createdAt = LocalDateTime.now()
            )
        }
    }
}
