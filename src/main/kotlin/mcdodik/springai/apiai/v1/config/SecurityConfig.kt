package mcdodik.springai.apiai.v1.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    @Value("\${app.security.enabled:true}") private val enabled: Boolean,
    @Value("\${app.security.required-scope:}") private val requiredScope: String,
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        if (!enabled) {
            return http.csrf { it.disable() }
                .authorizeExchange { it.anyExchange().permitAll() }
                .build()
        }

//        val jwtConverter = JwtAuthenticationConverter().apply {
//            setJwtGrantedAuthoritiesConverter { jwt ->
//
//                val scopes = (jwt.claims["scope"] as? String)
//                    ?.split(" ")
//                    ?.map { "SCOPE_$it" }
//                    ?: emptyList()
//                org.springframework.security.core.authority.SimpleGrantedAuthority::class
//                scopes.map { org.springframework.security.core.authority.SimpleGrantedAuthority(it) }
//            }
//        }

        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                it.pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // остальное — по scope
                if (requiredScope.isNotBlank()) {
                    it.anyExchange().hasAuthority("SCOPE_$requiredScope")
                } else {
                    it.anyExchange().authenticated()
                }
            }
//            .oauth2ResourceServer { rs ->
//                rs.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtConverter) }
//            }
            .build()
    }
}
