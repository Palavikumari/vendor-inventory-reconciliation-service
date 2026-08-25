package com.company.virs.parser;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private CsvParser csvParser;

    @BeforeEach
    void setUp() {
        csvParser = new CsvParser();
    }

    @Test
    void shouldParseValidCsv() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                V001,P001,Laptop,10,1000.50
                V001,P002,Mouse,20,25.75
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        List<InventoryRequest> result =
                csvParser.parse(file);

        assertEquals(2, result.size());

        assertEquals(
                "P001",
                result.get(0).getProductCode()
        );

        assertEquals(
                Integer.valueOf(10),
                result.get(0).getQuantity()
        );
    }

    @Test
    void shouldThrowExceptionWhenFileIsNull() {

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(null)
                );

        assertEquals(
                "CSV file cannot be empty.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        new byte[0]
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertEquals(
                "CSV file cannot be empty.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenRequiredHeaderMissing() {

        String csv = """
                vendorId,productCode,productName,quantity
                V001,P001,Laptop,10
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Missing required columns")
        );
    }

    @Test
    void shouldThrowExceptionWhenDuplicateProductCodeExists() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                V001,P001,Laptop,10,100
                V001,P001,Mouse,20,30
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Duplicate product code")
        );
    }

    @Test
    void shouldThrowExceptionForNegativeQuantity() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                V001,P001,Laptop,-10,100
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Quantity cannot be negative")
        );
    }

    @Test
    void shouldThrowExceptionForNegativeUnitPrice() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                V001,P001,Laptop,10,-100
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Unit price cannot be negative")
        );
    }

    @Test
    void shouldThrowExceptionForInvalidQuantity() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                V001,P001,Laptop,ABC,100
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid numeric value")
        );
    }

    @Test
    void shouldThrowExceptionForInvalidUnitPrice() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                V001,P001,Laptop,10,ABC
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid numeric value")
        );
    }

    @Test
    void shouldThrowExceptionWhenRequiredValueMissing() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                V001,,Laptop,10,100
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertTrue(
                exception.getMessage()
                        .contains("missing required value")
        );
    }

    @Test
    void shouldThrowExceptionWhenCsvContainsOnlyHeaders() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(file)
                );

        assertEquals(
                "CSV file does not contain any inventory records.",
                exception.getMessage()
        );
    }

    @Test
    void shouldTrimValuesSuccessfully() {

        String csv = """
                vendorId,productCode,productName,quantity,unitPrice
                 V001 , P001 , Laptop , 10 , 100.50
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        csv.getBytes(StandardCharsets.UTF_8)
                );

        List<InventoryRequest> result =
                csvParser.parse(file);

        assertEquals(
                "V001",
                result.get(0).getVendorId()
        );

        assertEquals(
                "P001",
                result.get(0).getProductCode()
        );
    }
}