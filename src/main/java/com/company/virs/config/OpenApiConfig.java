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
                                        "Vendor Inventory Reconciliation Service API"
                                )
                                .description(
                                        """
                                        Versioned REST API for the Vendor Inventory
                                        Reconciliation Service (VIRS).

                                        Capabilities:

                                        - Vendor inventory CSV ingestion
                                        - CSV validation
                                        - Object storage using MinIO
                                        - Controlled batch processing
                                        - Concurrent reconciliation
                                        - Reference inventory lookup
                                        - Reconciliation result tracking
                                        - Discrepancy notifications
                                        - Idempotent batch retrigger
                                        - JSON and CSV response representations
                                        - Historical backfill support
                                        """
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name(
                                                        "VIRS Engineering Team"
                                                )
                                                .email(
                                                        "engineering@company.com"
                                                )
                                )
                                .license(
                                        new License()
                                                .name("Internal Use")
                                )
                );
    }
}