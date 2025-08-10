package mcdodik.springai.apiai.v1.config

import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator

class BufferingExchangeDecorator(delegate: ServerWebExchange) : ServerWebExchangeDecorator(delegate) {
    private val responseDecorator = BufferingResponseDecorator(delegate)

    override fun getResponse(): ServerHttpResponse = responseDecorator

    fun responseDecorator(): BufferingResponseDecorator = responseDecorator
}
