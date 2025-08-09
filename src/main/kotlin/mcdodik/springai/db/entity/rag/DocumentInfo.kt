package mcdodik.springai.db.entity.rag

import mcdodik.springai.extensions.sha256
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data class representing information about a document stored in the system.
 *
 * This class holds metadata such as unique ID, file name, content type, hash, number of chunks,
 * creation timestamp, and an optional summary. It is used to store and manage document records in the database.
 */
data class DocumentInfo(
    val id: UUID = UUID.randomUUID(),
    val fileName: String,
    val extension: String,
    val hash: String,
    val chunkCount: Integer,
    val createdAt: LocalDateTime,
    val summary: String?,
) {
    companion object {
        /**
         * Creates a new [DocumentInfo] instance based on a [MultipartFile] and chunk count.
         *
         * @param file The uploaded file from which metadata will be extracted.
         * @param chunkCount The number of text chunks the file was split into.
         * @param summary Optional summary of the document (default is "empty").
         * @return A new [DocumentInfo] object with the specified properties.
         */
        fun createFromFileAndCunks(
            file: MultipartFile,
            chunkCount: Int,
            summary: String = "empty",
        ): DocumentInfo {
            return DocumentInfo(
                fileName = file.originalFilename.toString(),
                extension = file.contentType.toString(),
                hash = file.sha256(),
                chunkCount = chunkCount as Integer,
                createdAt = LocalDateTime.now(),
                summary = summary,
            )
        }
    }
}
