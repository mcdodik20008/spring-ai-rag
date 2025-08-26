package mcdodik.springai.apiai.v1.dto.prompt

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "Шаблон промпта")
data class PromptDto(
    @field:Schema(description = "ID промпта (nullable для create)", example = "prt_01HZ...7W3", nullable = true)
    val id: String?,

    @field:NotBlank
    @field:Schema(description = "Человекочитаемое имя", example = "Chunking RU v1")
    val name: String,

    @field:NotNull
    @field:Schema(description = "Тип промпта", implementation = PromptType::class, example = "CHUNKING")
    val type: PromptType,

    @field:NotBlank
    @field:Schema(description = "Текст шаблона (может содержать плейсхолдеры вида {{variable}})", example = "Ты — интеллектуальный редактор... {{domainName}} ...")
    val template: String,

    @field:Schema(description = "Список используемых плейсхолдеров", example = """["domainName","rules"]""")
    val variables: List<String> = emptyList(),

    @field:Schema(description = "Версия шаблона", example = "1", nullable = true)
    val version: Int? = null,

    @field:Schema(description = "Дата/время создания в ISO-8601", example = "2025-08-26T07:41:00Z", nullable = true)
    val createdAt: String? = null,
)

