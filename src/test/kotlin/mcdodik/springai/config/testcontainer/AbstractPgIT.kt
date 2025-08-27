package mcdodik.springai.config.testcontainer

import mcdodik.springai.config.plananalyzer.ExplainProxyDsPostProcessorConfig
import mcdodik.springai.config.plananalyzer.PlanAnalyzerTestConfig
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(
    ExplainProxyDsPostProcessorConfig::class,
    PlanAnalyzerTestConfig::class,
)
@TestPropertySource(
    properties = [
        "sql.explain.enabled=true",
        "sql.explain.whitelist.packages=mcdodik.springai", // твой корневой пакет
    ],
)
abstract class AbstractPgIT protected constructor() {
    companion object {
        @Container
        @JvmStatic
        val pg = PgTC // один и тот же синглтон

        @JvmStatic
        @BeforeAll
        fun startContainer() {
            if (!pg.isRunning) pg.start() // ← ключевая строка против "Mapped port..."
        }

        @JvmStatic
        @DynamicPropertySource
        fun props(reg: DynamicPropertyRegistry) {
            reg.add("spring.datasource.url") { pg.jdbcUrl }
            reg.add("spring.datasource.username") { pg.username }
            reg.add("spring.datasource.password") { pg.password }
            reg.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            reg.add("spring.flyway.enabled") { true }
            reg.add("spring.flyway.locations") { "classpath:db/migration" }
            reg.add("spring.sql.init.mode") { "never" }
        }
    }
}
