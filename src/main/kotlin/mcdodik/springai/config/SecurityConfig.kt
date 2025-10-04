package mcdodik.springai.config

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
            return http
                .csrf { it.disable() }
                .authorizeExchange { exchanges ->
                    exchanges
                        .pathMatchers(
                            "/favicon.ico",
                            "/assets/**",
                            "/webjars/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/actuator/health/**",
                        ).permitAll()
                        .anyExchange()
                        .permitAll()
                }
                // НИЧЕГО не трогаем про session/request cache/security context — пусть будут дефолты
                .build()
        }

        // Если используешь JWT и SCOPE-проверку — раскомментируй блок ниже и настрой issuer/jwk в application.yml
        // val jwtConverter = JwtAuthenticationConverter().apply {
        //     setJwtGrantedAuthoritiesConverter { jwt ->
        //         val scopes = (jwt.claims["scope"] as? String)
        //             ?.split(" ")
        //             ?.map { "SCOPE_$it" }
        //             ?: emptyList()
        //         scopes.map { org.springframework.security.core.authority.SimpleGrantedAuthority(it) }
        //     }
        // }

        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(
                        "/favicon.ico",
                        "/assets/**",
                        "/webjars/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health/**",
                    ).permitAll()
                    .apply {
                        if (requiredScope.isNotBlank()) {
                            anyExchange().hasAuthority("SCOPE_$requiredScope")
                        } else {
                            anyExchange().authenticated()
                        }
                    }
            }
            // .oauth2ResourceServer { rs ->
            //     rs.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtConverter) }
            // }
            // ВАЖНО: не ставим NoOpServerSecurityContextRepository и не отключаем requestCache —
            // иначе сессии станут stateless и Redis Session не будет работать как ожидается.
            .build()
    }
}
