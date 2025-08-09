package mcdodik.springai.config.interceptor

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val delay: Long = 500L,
) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                return execution.execute(request, body)
            } catch (ex: Exception) {
                lastException = ex
                Thread.sleep(delay)
                attempt++
            }
        }

        throw lastException ?: IllegalStateException("Unknown restTemplate failure")
    }
}
