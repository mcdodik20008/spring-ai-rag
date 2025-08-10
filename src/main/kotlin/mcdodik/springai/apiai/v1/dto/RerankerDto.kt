package mcdodik.springai.apiai.v1.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class RerankerDto(
    @field:NotBlank val name: String,
    @field:Positive @field:Max(100) val topRerank: Int = 5,
)
