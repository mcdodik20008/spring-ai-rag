package mcdodik.springai

import mcdodik.springai.advisors.config.VectorAdvisorProperties
import mcdodik.springai.api.config.OpenRouterProperties
import mcdodik.springai.scheduling.config.DedupProperties
import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    OpenRouterProperties::class,
    VectorAdvisorProperties::class,
    DedupProperties::class,
)
@MapperScan(
    basePackages = [
        "mcdodik.springai.db.mybatis.mapper",
        "mcdodik.springai.scheduling.mapper",
    ],
)
class SpringAiApplication

fun main(args: Array<String>) {
    runApplication<SpringAiApplication>(*args)
}
