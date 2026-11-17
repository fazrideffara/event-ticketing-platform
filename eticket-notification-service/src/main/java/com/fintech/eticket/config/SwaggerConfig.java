package com.fintech.eticket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Ticket Fulfillment & Notification Service API")
                        .version("1.0.0")
                        .description("Worker microservice that consumes Kafka events, generates QR verification hashes, and dispatches electronic tickets.")
                        .contact(new Contact()
                                .name("Muhammad Fazri Deffara Riswandy")
                                .email("fazri.deffara@example.com")));
    }
}
