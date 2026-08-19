package com.company.virs.parser;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserImplTest {

    private final CsvParserImpl csvParser =
            new CsvParserImpl();

    private MockMultipartFile csv(String content) {

        return new MockMultipartFile(
                "file",
                "inventory.csv",
                "text/csv",
                content.getBytes()
        );
    }

    @Test
    void shouldParseValidCsv() {

        String content =
                """
                vendorId,sku,productName,quantity,unitPrice
                VENDOR001,SKU1001,Laptop,100,25000.00
                VENDOR002,SKU1002,Monitor,50,12000.00
                """;

        List<InventoryRequest> result =
                csvParser.parse(csv(content));

        assertEquals(2, result.size());

        assertEquals(
                "VENDOR001",
                result.get(0).getVendorId()
        );

        assertEquals(
                "SKU1001",
                result.get(0).getSku()
        );

        assertEquals(
                "Laptop",
                result.get(0).getProductName()
        );

        assertEquals(
                100,
                result.get(0).getQuantity()
        );

        assertEquals(
                new BigDecimal("25000.00"),
                result.get(0).getUnitPrice()
        );
    }

    @Test
    void shouldRejectMissingHeader() {

        String content =
                """
                vendorId,sku,productName,quantity
                VENDOR001,SKU1001,Laptop,100
                """;

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(csv(content))
                );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid CSV headers")
        );
    }

    @Test
    void shouldRejectMissingRequiredValue() {

        String content =
                """
                vendorId,sku,productName,quantity,unitPrice
                VENDOR001,,Laptop,100,25000
                """;

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(csv(content))
                );

        assertTrue(
                exception.getMessage()
                        .contains("missing required values")
        );
    }

    @Test
    void shouldRejectInvalidQuantity() {

        String content =
                """
                vendorId,sku,productName,quantity,unitPrice
                VENDOR001,SKU1001,Laptop,abc,25000
                """;

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(csv(content))
                );

        assertTrue(
                exception.getMessage()
                        .contains("Invalid numeric value")
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        String content =
                """
                vendorId,sku,productName,quantity,unitPrice
                VENDOR001,SKU1001,Laptop,-10,25000
                """;

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(csv(content))
                );

        assertTrue(
                exception.getMessage()
                        .contains("Quantity cannot be negative")
        );
    }

    @Test
    void shouldRejectNegativeUnitPrice() {

        String content =
                """
                vendorId,sku,productName,quantity,unitPrice
                VENDOR001,SKU1001,Laptop,10,-25000
                """;

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> csvParser.parse(csv(content))
                );

        assertTrue(
                exception.getMessage()
                        .contains("Unit price cannot be negative")
        );
    }

    @Test
    void shouldIgnoreEmptyLines() {

        String content =
                """
                vendorId,sku,productName,quantity,unitPrice
                VENDOR001,SKU1001,Laptop,100,25000

                VENDOR002,SKU1002,Monitor,50,12000
                """;

        List<InventoryRequest> result =
                csvParser.parse(csv(content));

        assertEquals(2, result.size());
    }

    @Test
    void shouldTrimCsvValues() {

        String content =
                """
                vendorId,sku,productName,quantity,unitPrice
                 VENDOR001 , SKU1001 , Laptop , 100 , 25000
                """;

        List<InventoryRequest> result =
                csvParser.parse(csv(content));

        assertEquals(
                "VENDOR001",
                result.get(0).getVendorId()
        );

        assertEquals(
                "SKU1001",
                result.get(0).getSku()
        );
    }
}