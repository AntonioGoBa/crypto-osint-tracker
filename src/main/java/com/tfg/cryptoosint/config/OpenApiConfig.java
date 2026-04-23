package com.tfg.cryptoosint.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.title:Crypto OSINT Tracker API}")
    private String title;

    @Value("${app.openapi.description:API para trazado y análisis OSINT de transacciones Bitcoin}")
    private String description;

    @Value("${app.openapi.version:1.0.0}")
    private String version;

    @Value("${app.openapi.contact.name:TFG}")
    private String contactName;

    @Value("${app.openapi.contact.email:}")
    private String contactEmail;

    @Value("${app.openapi.contact.url:}")
    private String contactUrl;

    @Value("${app.openapi.license.name:Apache 2.0}")
    private String licenseName;

    @Value("${app.openapi.license.url:https://www.apache.org/licenses/LICENSE-2.0.html}")
    private String licenseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)));
    }
}