package mcdodik.springai.apiai.v1.config

import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream

class BufferingResponseDecorator(exchange: ServerWebExchange) :
    ServerHttpResponseDecorator(exchange.response) {
    private val baos = ByteArrayOutputStream()
    private var status: HttpStatus? = null

    fun bodyBytes(): ByteArray = baos.toByteArray()

    fun statusCode(): HttpStatusCode = status ?: super.getStatusCode() ?: HttpStatus.OK

    fun setStatusCode(status: HttpStatus?): Boolean {
        this.status = status
        return super.setStatusCode(status)
    }

    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        val flux =
            Flux.from(body).map { data ->
                val bytes = ByteArray(data.readableByteCount())
                data.read(bytes)
                DataBufferUtils.release(data)
                baos.write(bytes)
                bufferFactory().wrap(bytes)
            }
        return super.writeWith(flux)
    }

    override fun writeAndFlushWith(body: Publisher<out Publisher<out DataBuffer>>): Mono<Void> {
        val flux =
            Flux.from(body).flatMap { publisher ->
                Flux.from(publisher).map { data ->
                    val bytes = ByteArray(data.readableByteCount())
                    data.read(bytes)
                    DataBufferUtils.release(data)
                    baos.write(bytes)
                    bufferFactory().wrap(bytes)
                }
            }
        return super.writeAndFlushWith(Flux.just(flux))
    }
}
