package com.company.virs.validation;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InventoryValidationTest {

    private final InventoryValidation inventoryValidation =
            new InventoryValidation();

    private InventoryRequest validRequest() {

        return InventoryRequest.builder()
                .vendorId("VENDOR001")
                .sku("SKU1001")
                .productName("Laptop")
                .quantity(100)
                .unitPrice(new BigDecimal("25000.00"))
                .build();
    }

    @Test
    void shouldPassForValidInventory() {

        assertDoesNotThrow(
                () -> inventoryValidation.validate(validRequest())
        );
    }

    @Test
    void shouldRejectNullRequest() {

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(null)
                );

        assertEquals(
                "Inventory request cannot be null.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingVendorId() {

        InventoryRequest request = validRequest();
        request.setVendorId("");

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(request)
                );

        assertEquals(
                "Vendor Id is mandatory.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingSku() {

        InventoryRequest request = validRequest();
        request.setSku("");

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(request)
                );

        assertEquals(
                "SKU is mandatory.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectMissingProductName() {

        InventoryRequest request = validRequest();
        request.setProductName("");

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(request)
                );

        assertEquals(
                "Product Name is mandatory.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullQuantity() {

        InventoryRequest request = validRequest();
        request.setQuantity(null);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(request)
                );

        assertEquals(
                "Quantity must be zero or greater.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNegativeQuantity() {

        InventoryRequest request = validRequest();
        request.setQuantity(-1);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(request)
                );

        assertEquals(
                "Quantity must be zero or greater.",
                exception.getMessage()
        );
    }

    @Test
    void shouldAcceptZeroQuantity() {

        InventoryRequest request = validRequest();
        request.setQuantity(0);

        assertDoesNotThrow(
                () -> inventoryValidation.validate(request)
        );
    }

    @Test
    void shouldRejectNullUnitPrice() {

        InventoryRequest request = validRequest();
        request.setUnitPrice(null);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(request)
                );

        assertEquals(
                "Unit Price cannot be negative.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNegativeUnitPrice() {

        InventoryRequest request = validRequest();
        request.setUnitPrice(new BigDecimal("-10.00"));

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> inventoryValidation.validate(request)
                );

        assertEquals(
                "Unit Price cannot be negative.",
                exception.getMessage()
        );
    }

    @Test
    void shouldAcceptZeroUnitPrice() {

        InventoryRequest request = validRequest();
        request.setUnitPrice(BigDecimal.ZERO);

        assertDoesNotThrow(
                () -> inventoryValidation.validate(request)
        );
    }
}