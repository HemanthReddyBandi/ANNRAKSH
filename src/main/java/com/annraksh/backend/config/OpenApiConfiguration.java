package com.annraksh.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI annrakshOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ANNRAKSH API")
                        .version("1.0.0")
                        .description("REST API for demand-aware crop planning, cultivation tracking, and explainable recommendations. "
                                + "Protected operations require a JWT bearer token with the appropriate FARMER or ADMIN role."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
