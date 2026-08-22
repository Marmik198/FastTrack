package com.karmayuva.pointsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Serves interactive API docs at /swagger-ui.html (raw spec at /v3/api-docs).
 *
 * To call admin endpoints from the Swagger UI: POST /api/admin/auth/login first to get
 * a token, then click "Authorize" at the top of the page and paste the token in as
 * `Bearer <token>` (or just the raw token, depending on springdoc version prompt) -
 * it will then be attached automatically to every subsequent request you try.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI karmaYuvaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FastTrack API")
                        .description("Backend API for FastTrack - Karma Yuva Group's system for managing "
                                + "children's fast/tap point accrual, gifting campaigns, and redemption approvals.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Karma Yuva Group")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by POST /api/admin/auth/login")));
    }
}
