package mcdodik.springai.openapi.dto

import io.swagger.v3.oas.annotations.media.Schema
import mcdodik.springai.apiai.v1.dto.chat.ChunkDto

@Schema(description = "Страница чанков (обёртка для UI Swagger)")
data class PageChunkDto(
    @Schema(description = "Элементы") val items: List<ChunkDto>,
    @Schema(description = "Всего") val total: Long,
    @Schema(description = "Смещение") val offset: Int,
    @Schema(description = "Лимит") val limit: Int
)
