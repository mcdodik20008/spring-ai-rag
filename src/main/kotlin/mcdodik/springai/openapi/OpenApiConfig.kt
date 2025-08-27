package mcdodik.springai.openapi

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val bearer = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")

        return OpenAPI()
            .components(Components().addSecuritySchemes("bearerAuth", bearer))
//            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            .info(
                Info()
                    .title("RAG Service API") // общий заголовок по умолчанию
                    .version("v1")
                    .description("Глобальная спецификация сервиса RAG")
            )
    }

}
