package com.company.virs.validation;

import com.company.virs.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class CsvValidationTest {

    private final CsvValidation csvValidation =
            new CsvValidation();

    @Test
    void shouldAcceptValidCsvFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "vendorId,sku,productName,quantity,unitPrice\n"
                                .getBytes()
                );

        assertDoesNotThrow(
                () -> csvValidation.validate(file)
        );
    }

    @Test
    void shouldRejectNullFile() {

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvValidation.validate(null)
                );

        assertEquals(
                "CSV file cannot be empty.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        new byte[0]
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvValidation.validate(file)
                );

        assertEquals(
                "CSV file cannot be empty.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNonCsvFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "test".getBytes()
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvValidation.validate(file)
                );

        assertEquals(
                "Invalid file type. Only CSV files are allowed.",
                exception.getMessage()
        );
    }

    @Test
    void shouldAcceptUpperCaseCsvExtension() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.CSV",
                        "text/csv",
                        "test".getBytes()
                );

        assertDoesNotThrow(
                () -> csvValidation.validate(file)
        );
    }
}