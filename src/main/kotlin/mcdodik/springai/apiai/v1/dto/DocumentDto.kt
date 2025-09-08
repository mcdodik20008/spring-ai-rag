package mcdodik.springai.apiai.v1.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Документ")
data class DocumentDto(
    @field:Schema(description = "ID документа", example = "doc_01HZX6Z7Z6W0R7W3E2KQ1VQ1V4")
    val id: String,
    @field:Schema(description = "ID базы знаний", example = "kb-legal-ru")
    val kbId: String,
    @field:Schema(description = "Имя файла", example = "contract_2024_07.pdf")
    val fileName: String,
    @field:Schema(description = "MIME-тип", example = "application/pdf", nullable = true)
    val mime: String?,
    @field:Schema(description = "Размер файла в байтах", example = "1280456", nullable = true)
    val size: Long?,
    @field:Schema(description = "Хэш содержимого (hex)", example = "a3f1c2ef...", nullable = true)
    val hash: String?,
    @field:Schema(description = "Статус индексации", example = "READY")
    val status: String,
    @field:Schema(description = "Дата/время создания ISO-8601", example = "2025-08-26T07:41:00Z")
    val createdAt: String,
    @field:Schema(description = "Краткое резюме", nullable = true, example = "Договор поставки, штрафы 0.1%/день")
    val summary: String?,
    @field:Schema(description = "Теги", nullable = true, example = """["contract","penalties"]""")
    val tags: List<String>?,
)
