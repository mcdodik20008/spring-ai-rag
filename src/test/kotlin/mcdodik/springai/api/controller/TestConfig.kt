package mcdodik.springai.api.controller

import io.mockk.mockk
import mcdodik.springai.rag.service.RagService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {
    @Bean
    fun ragService(): RagService = mockk(relaxed = true)
}
