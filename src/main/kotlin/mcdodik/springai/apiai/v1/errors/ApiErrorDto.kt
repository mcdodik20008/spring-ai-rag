package mcdodik.springai.apiai.v1.errors

data class ApiErrorDto(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null,
    val traceId: String? = null,
)
