package mcdodik.springai.openapi.dto

import io.swagger.v3.oas.annotations.media.Schema
import mcdodik.springai.apiai.v1.dto.DocumentDto

@Schema(description = "Страница документов (обёртка для UI Swagger)")
data class PageDocumentDto(
    @Schema(description = "Элементы") val items: List<DocumentDto>,
    @Schema(description = "Всего") val total: Long,
    @Schema(description = "Смещение") val offset: Int,
    @Schema(description = "Лимит") val limit: Int
)

