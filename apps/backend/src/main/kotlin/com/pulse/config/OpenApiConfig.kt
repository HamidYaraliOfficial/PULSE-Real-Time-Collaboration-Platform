package com.pulse.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun pulseOpenApi(): OpenAPI = OpenAPI()
        .info(Info().title("PULSE API").version("v1").description("Real-Time Collaboration Platform API"))
        .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
        .components(
            io.swagger.v3.oas.models.Components().addSecuritySchemes(
                "bearerAuth",
                SecurityScheme().name("bearerAuth").type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
            )
        )
}
