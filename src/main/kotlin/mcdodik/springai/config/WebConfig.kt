package mcdodik.springai.config

import mcdodik.springai.config.interceptor.RetryInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * Configuration class for web-related beans.
 * This class is used to define and configure beans related to HTTP communication in the application.
 */
@Configuration
class WebConfig {

    /**
     * Bean definition for a configured [RestTemplate] instance.
     * The [RestTemplate] is enhanced with an interceptor that enables retry logic on failed requests.
     *
     * @param builder The [RestTemplateBuilder] used to create and configure the [RestTemplate].
     * @return A configured [RestTemplate] instance with a retry interceptor added.
     */
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .additionalInterceptors(RetryInterceptor(maxRetries = 3))
            .build()
    }

}
