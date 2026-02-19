package com.ssak.ssak.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        String jwt = "JWT";

        // 1. API 요청 시 사용할 보안 요구사항 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        // 2. JWT 보안 스키마 정의 (Bearer 방식)
        Components components = new Components()
                .addSecuritySchemes(jwt, new SecurityScheme()
                        .name("jwt")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
        return new OpenAPI()
                .info(new Info()
                        .title("Seederslab API Document")
                        .description("SeedersLab API 명세서")
                        .version("1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
