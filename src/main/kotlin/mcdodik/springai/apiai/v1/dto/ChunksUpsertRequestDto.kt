package mcdodik.springai.apiai.v1.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Запрос upsert чанков")
data class ChunksUpsertRequestDto(
    @field:NotBlank
    @field:Schema(description = "ID документа", example = "doc_01HZX6Z7Z6W0R7W3E2KQ1VQ1V4")
    val docId: String,
    @field:Size(min = 1, max = 10000)
    @field:Schema(description = "Список чанков (1..10000)")
    val chunks: List<UpsertChunkDto>,
    @field:Schema(description = "Вычислить эмбеддинги для тех, у кого отсутствуют", defaultValue = "true", example = "true")
    val embedIfMissing: Boolean = true,
)
