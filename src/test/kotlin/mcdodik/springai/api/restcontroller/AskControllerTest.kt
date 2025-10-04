package mcdodik.springai.api.restcontroller

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import mcdodik.springai.api.dto.ask.AskRequest
import mcdodik.springai.rag.service.api.RagService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux

@WebFluxTest(controllers = [AskController::class])
@Import(AskControllerTest.TestConfig::class, AskControllerTest.PermitAllSecurity::class)
@AutoConfigureRestDocs
@AutoConfigureWebTestClient
@ExtendWith(org.springframework.restdocs.RestDocumentationExtension::class)
@TestPropertySource(
    properties = [
        "logging.level.org.springframework.web=DEBUG",
        "logging.level.org.springframework.web.reactive.result.method.annotation=DEBUG",
    ],
)
class AskControllerTest {
    @Autowired
    lateinit var ragService: RagService

    @Autowired
    lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp(restDocs: RestDocumentationContextProvider) {
        // только конфиг REST Docs как фильтр; document(...) используем в consumeWith ниже
        webTestClient =
            webTestClient
                .mutate()
                .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocs))
                .build()
    }

    @Test
    fun `docs - SSE`() {
        val q = "Кто такой Глен Гульд?"
        every { ragService.ask(q) } returns Flux.just("Глен ", "Гульд", " — ", "пианист")

        webTestClient
            .post()
            .uri("/api/ask")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(AskRequest(q, 123L, 123L))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .expectBody()
            .consumeWith(
                WebTestClientRestDocumentation.document(
                    "ask-sse",
                    requestFields(fieldWithPath("question").description("Вопрос пользователя")),
                    responseHeaders(headerWithName("Content-Type").description("text/event-stream; charset=UTF-8")),
                ),
            )
        verify(exactly = 1) { ragService.ask(q) }
    }

    @Test
    fun `behavior - returns expected chunks`() {
        val q = "Кто такой Глен Гульд?"
        val chunks = listOf("Глен ", "Гульд", " — ", "пианист")
        every { ragService.ask(q) } returns Flux.fromIterable(chunks)

        val body: List<String> =
            webTestClient
                .post()
                .uri("/api/rag/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(AskRequest(q, 123L, 123L))
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(String::class.java)
                .returnResult()
                .responseBody!!

        assertThat(body).isEqualTo(chunks)
        verify(exactly = 1) { ragService.ask(q) }
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun ragService(): RagService = mockk(relaxed = true)
    }

    @Configuration
    class PermitAllSecurity {
        @Bean
        fun securityWebFilterChain(http: org.springframework.security.config.web.server.ServerHttpSecurity) = http.csrf { it.disable() }.authorizeExchange { it.anyExchange().permitAll() }.build()
    }
}
