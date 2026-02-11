package com.shotaroi.featureflags.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String basicAuth = "basicAuth";
        final String apiKey = "apiKey";

        return new OpenAPI()
                .info(new Info()
                        .title("Feature Flag API")
                        .version("1.0")
                        .description("""
                                **Evaluation** (`/api/flags/**`): use **X-API-Key** header. \\
                                **Admin** (`/api/admin/**`): use **HTTP Basic** (e.g. admin / admin123).
                                """))
                .addSecurityItem(new SecurityRequirement().addList(apiKey))
                .addSecurityItem(new SecurityRequirement().addList(basicAuth))
                .components(new Components()
                        .addSecuritySchemes(basicAuth,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("Admin endpoints: use HTTP Basic (e.g. admin / admin123)"))
                        .addSecuritySchemes(apiKey,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(ApiKeyAuthenticationFilter.API_KEY_HEADER)
                                        .description("Evaluation endpoint: use X-API-Key header")));
    }
}
