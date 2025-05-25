package mcdodik.springai.config

import mcdodik.springai.config.interceptor.RetryInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class WebConfig {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .additionalInterceptors(RetryInterceptor(maxRetries = 3))
            .build()
    }

}