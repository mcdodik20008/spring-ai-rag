package mcdodik.springai.config.plananalyzer

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration(proxyBeanMethods = false)
class PlanAnalyzerTestConfig {
    @Bean
    fun filesystemPlanSink(): PlanSink = FilesystemPlanSink()
}
