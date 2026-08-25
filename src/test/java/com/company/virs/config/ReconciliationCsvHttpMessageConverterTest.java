package com.company.virs.config;

import com.company.virs.dto.response.ReconciliationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpOutputMessage;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReconciliationCsvHttpMessageConverterTest {

    @Test
    void writeCsv() throws Exception {

        ReconciliationCsvHttpMessageConverter converter =
                new ReconciliationCsvHttpMessageConverter();

        ReconciliationResponse response =
                ReconciliationResponse.builder()
                        .batchId(UUID.randomUUID())
                        .fileName("inventory.csv")
                        .executionType("INITIAL")
                        .status("COMPLETED")
                        .build();

        MockHttpOutputMessage output =
                new MockHttpOutputMessage();

        converter.write(
                response,
                MediaType.valueOf("text/csv"),
                output
        );

        String csv =
                output.getBodyAsString();

        assertTrue(
                csv.contains("inventory.csv")
        );
    }

    @Test
    void canWriteCsv() {

        ReconciliationCsvHttpMessageConverter converter =
                new ReconciliationCsvHttpMessageConverter();

        assertTrue(
                converter.canWrite(
                        ReconciliationResponse.class,
                        MediaType.valueOf("text/csv")
                )
        );
    }
}