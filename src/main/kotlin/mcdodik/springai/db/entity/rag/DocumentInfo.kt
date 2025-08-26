package mcdodik.springai.db.entity.rag

import io.swagger.v3.oas.annotations.media.Schema
import mcdodik.springai.extensions.sha256
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

@Schema(description = "Метаданные о документе, попавшем в индекс")
data class DocumentInfo(
    @field:Schema(
        description = "Уникальный идентификатор документа (UUID v4)",
        format = "uuid",
        example = "5b1f3e0b-9c5d-4a07-8f3a-0d7c9a2b9e11"
    )
    val id: UUID = UUID.randomUUID(),

    @field:Schema(
        description = "Имя исходного файла без пути",
        example = "invoice_2024_07_15.pdf"
    )
    val fileName: String,

    @field:Schema(
        description = "Расширение файла",
        example = "pdf"
    )
    val extension: String,

    @field:Schema(
        description = "Хэш содержимого (например, SHA-256, hex)",
        example = "a3f1c2...ef9b"
    )
    val hash: String,

    @field:Schema(
        description = "Количество чанков, на которое был разбит файл при индексации",
        example = "12",
        minimum = "0"
    )
    val chunkCount: Int,

    @field:Schema(
        description = "Дата/время индексации (локальное)",
        format = "date-time",
        example = "2025-08-20T14:23:11"
    )
    val createdAt: LocalDateTime,

    @field:Schema(
        description = "Краткая аннотация/резюме содержимого",
        example = "Инвойс за июль 2024 г., сумма 124 500 ₽, контрагент ООО «Ромашка»"
    )
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
        ): DocumentInfo =
            DocumentInfo(
                fileName = file.originalFilename.toString(),
                extension = file.contentType.toString(),
                hash = file.sha256(),
                chunkCount = chunkCount,
                createdAt = LocalDateTime.now(),
                summary = summary,
            )
    }
}
