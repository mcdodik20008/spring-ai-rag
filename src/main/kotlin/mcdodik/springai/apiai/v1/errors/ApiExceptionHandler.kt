package mcdodik.springai.apiai.v1.errors

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException

@ControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(e: WebExchangeBindException): ResponseEntity<ApiErrorDto> {
        val details = e.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        val body =
            ApiErrorDto(
                code = "VALIDATION_ERROR",
                message = "Request validation failed",
                details = details,
                traceId = null,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleStatus(e: ResponseStatusException): ResponseEntity<ApiErrorDto> {
        val status = e.statusCode
        val code =
            when (status) {
                is org.springframework.http.HttpStatus -> status.name //  e.g. BAD_REQUEST
                else -> "HTTP_${status.value()}"
            }
        val body =
            ApiErrorDto(
                code = code,
                message = e.reason ?: e.message,
                details = null,
                traceId = null,
            )
        return ResponseEntity.status(status).body(body)
    }

    @ExceptionHandler(Throwable::class)
    fun handleOther(e: Throwable): ResponseEntity<ApiErrorDto> {
        val body =
            ApiErrorDto(
                code = "INTERNAL",
                message = "Internal server error",
                details = mapOf("type" to e::class.simpleName),
                traceId = null,
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
