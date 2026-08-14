package com.company.virs.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Vendor Inventory Reconciliation Service API")
                                .description(
                                        "API for uploading vendor inventory files, batch reconciliation and discrepancy reporting.")
                                .version("v1")
                                .contact(
                                        new Contact()
                                                .name("Engineering Team")
                                                .email("engineering@company.com"))
                );
    }
}