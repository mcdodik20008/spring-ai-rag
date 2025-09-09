package mcdodik.springai

import mcdodik.springai.advisors.config.VectorAdvisorProperties
import mcdodik.springai.api.config.OpenRouterProperties
import mcdodik.springai.scheduling.config.DedupProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    OpenRouterProperties::class,
    VectorAdvisorProperties::class,
    DedupProperties::class,
)
class SpringAiApplication

fun main(args: Array<String>) {
    runApplication<SpringAiApplication>(*args)
}
