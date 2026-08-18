package com.company.virs.config;

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
                                .title(
                                        "Vendor Inventory Reconciliation Service API")
                                .description(
                                        """
                                        REST API for the Vendor Inventory Reconciliation Service (VIRS).

                                        The service:
                                        - Ingests vendor inventory CSV files
                                        - Validates and parses vendor inventory data
                                        - Stores inventory data in PostgreSQL
                                        - Reconciles vendor inventory against a reference inventory source
                                        - Tracks reconciliation results
                                        - Publishes discrepancy notifications
                                        - Supports manual batch retry
                                        - Supports JSON and CSV response representations
                                        """)
                                .version("v1.0.0")
                                .contact(
                                        new Contact()
                                                .name("VIRS Engineering Team")
                                                .email("engineering@company.com"))
                                .license(
                                        new License()
                                                .name("Internal Use")));
    }
}