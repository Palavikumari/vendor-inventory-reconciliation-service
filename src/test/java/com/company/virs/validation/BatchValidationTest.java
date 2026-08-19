package com.company.virs.validation;

import com.company.virs.dto.request.BatchRequest;
import com.company.virs.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatchValidationTest {

    private final BatchValidation batchValidation =
            new BatchValidation();

    @Test
    void shouldPassForValidCsvBatchRequest() {

        BatchRequest request = BatchRequest.builder()
                .fileName("inventory.csv")
                .executionType("INITIAL")
                .build();

        assertDoesNotThrow(
                () -> batchValidation.validate(request)
        );
    }

    @Test
    void shouldRejectNullRequest() {

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> batchValidation.validate(null)
                );

        assertEquals(
                "Batch request cannot be null.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectBlankFileName() {

        BatchRequest request = BatchRequest.builder()
                .fileName(" ")
                .executionType("INITIAL")
                .build();

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> batchValidation.validate(request)
                );

        assertEquals(
                "File name is required.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingExecutionType() {

        BatchRequest request = BatchRequest.builder()
                .fileName("inventory.csv")
                .executionType(" ")
                .build();

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> batchValidation.validate(request)
                );

        assertEquals(
                "Execution type is required.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNonCsvFile() {

        BatchRequest request = BatchRequest.builder()
                .fileName("inventory.xlsx")
                .executionType("INITIAL")
                .build();

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> batchValidation.validate(request)
                );

        assertEquals(
                "Only CSV files are supported.",
                exception.getMessage()
        );
    }

    @Test
    void shouldAcceptUpperCaseCsvExtension() {

        BatchRequest request = BatchRequest.builder()
                .fileName("inventory.CSV")
                .executionType("INITIAL")
                .build();

        assertDoesNotThrow(
                () -> batchValidation.validate(request)
        );
    }
}