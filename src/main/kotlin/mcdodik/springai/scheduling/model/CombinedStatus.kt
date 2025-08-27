package mcdodik.springai.scheduling.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Сводка по нескольким операциям")
data class CombinedStatus(
    @field:Schema(description = "Результаты операций")
    val results: List<OpStatus>,
    @field:Schema(description = "Суммарная длительность, мс", example = "55861", minimum = "0")
    val totalDurationMs: Long,
)
