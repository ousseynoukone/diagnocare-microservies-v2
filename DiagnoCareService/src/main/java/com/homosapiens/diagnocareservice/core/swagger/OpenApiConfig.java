package com.homosapiens.diagnocareservice.core.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api/v1/diagnocare")
                                .description("Local DiagnoCare Service"),
                        new Server()
                                .url("http://localhost:8765/api/v1/diagnocare")
                                .description("Gateway")
                ))
                .info(new Info()
                        .title("DiagnoCare Service API")
                        .description("This is the main service for the DiagnoCare Platform")
                        .version("1.0")
                        .contact(new Contact()
                                .name("DiagnoCare Team")
                                .email("support@diagnocare.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
} 