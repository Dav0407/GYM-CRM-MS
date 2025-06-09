package com.epam.gym_crm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Development Server");

        Server localServer = new Server()
                .url("http://localhost:9000")
                .description("Local Server");

        Server stagingServer = new Server()
                .url("http://localhost:9090")
                .description("Staging Server");

        Server prodServer = new Server()
                .url("http://localhost:8000")
                .description("Production Server");

        Contact contact = new Contact()
                .name("Gym CRM API")
                .email("support@gymcrm.com")
                .url("https://www.gymcrm.com");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Gym CRM API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for managing gym trainers, trainees, and training sessions.")
                .license(license);

        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, localServer, stagingServer, prodServer))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
