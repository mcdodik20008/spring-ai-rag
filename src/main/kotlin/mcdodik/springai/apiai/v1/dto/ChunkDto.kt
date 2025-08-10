package mcdodik.springai.apiai.v1.dto

import com.fasterxml.jackson.annotation.JsonInclude

data class ChunkDto(
    val id: String,
    val docId: String,
    val idx: Int,
    val content: String,
    val tokens: Int?,
    val meta: Map<String, Any?>? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val score: Double? = null,
)
