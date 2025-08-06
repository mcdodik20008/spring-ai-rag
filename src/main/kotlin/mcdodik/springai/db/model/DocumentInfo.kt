package mcdodik.springai.db.model

import java.time.LocalDateTime
import java.util.UUID
import mcdodik.springai.extension.sha256
import org.springframework.web.multipart.MultipartFile

data class DocumentInfo(
    val id: UUID = UUID.randomUUID(),
    val fileName: String,
    val extension: String,
    val hash: String,
    val chunkCount: Integer,
    val createdAt: LocalDateTime,
    val summary: String?
) {
    companion object {
        fun createFromFileAndCunks(
            file: MultipartFile,
            chunkCount: Int,
            summary: String = "empty"
        ): DocumentInfo {
            return DocumentInfo(
                fileName = file.originalFilename.toString(),
                extension = file.contentType.toString(),
                hash = file.sha256(),
                chunkCount = chunkCount as Integer,
                createdAt = LocalDateTime.now(),
                summary = summary
            )
        }
    }
}

