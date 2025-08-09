package mcdodik.springai

import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.config.chatmodel.OpenRouterProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    OpenRouterProperties::class,
    VectorAdvisorProperties::class,
)
class SpringAiApplication

fun main(args: Array<String>) {
    runApplication<SpringAiApplication>(*args)
}
