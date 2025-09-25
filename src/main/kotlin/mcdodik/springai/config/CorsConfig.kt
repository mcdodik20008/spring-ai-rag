package mcdodik.springai.config // Kotlin / Spring Boot
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
//    @Bean
//    fun corsWebFilter(): CorsWebFilter {
//        val cfg = CorsConfiguration().apply {
//            // Локально можно так, в проде — сузить
//            allowedOriginPatterns = listOf("*")
//            allowedMethods = listOf("GET", "POST", "OPTIONS")
//            allowedHeaders = listOf("*")
//            allowCredentials = false
//            maxAge = 3600
//        }
//
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/api/**", cfg)
//        source.registerCorsConfiguration("/chat/**", cfg)
//        return CorsWebFilter(source)
//    }
}
