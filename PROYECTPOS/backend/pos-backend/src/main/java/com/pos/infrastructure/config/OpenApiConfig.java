package com.pos.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI posOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("POS API - Sistema de Punto de Venta")
                .description("API REST para el sistema de punto de venta con arquitectura hexagonal")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Equipo de Desarrollo POS")
                    .email("soporte@pos-system.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(java.util.List.of(
                new Server()
                    .url("http://localhost:" + serverPort + "/api/v1")
                    .description("Servidor de desarrollo"),
                new Server()
                    .url("https://api.pos-system.com/api/v1")
                    .description("Servidor de producción")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token obtenido del endpoint /auth/login")))
            .security(java.util.List.of(
                new SecurityRequirement().addList("bearerAuth")));
    }
}