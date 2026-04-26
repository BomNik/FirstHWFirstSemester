package com.mipt.nikitabumagin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoListOpenApi(@Value("${app.api-version}") String apiVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("To-Do List API")
                        .version(apiVersion)
                        .description("REST API for managing tasks, attachments, favorites, "
                                + "preferences, and demo endpoints.")
                        .contact(new Contact()
                                .name("Educational project API")));
    }
}
