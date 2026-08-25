package com.company.virs.validation;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class InventoryValidationTest {

    private InventoryValidation validation;

    @BeforeEach
    void setUp() {
        validation = new InventoryValidation();
    }

    private InventoryRequest buildValidRequest() {

        return InventoryRequest.builder()
                .vendorId("V001")
                .productCode("P001")
                .productName("Laptop")
                .quantity(10)
                .unitPrice(BigDecimal.TEN)
                .build();
    }

    @Test
    void validate_ShouldPass_WhenRequestIsValid() {

        InventoryRequest request =
                buildValidRequest();

        assertDoesNotThrow(
                () -> validation.validate(request)
        );
    }

    @Test
    void validate_ShouldThrow_WhenRequestIsNull() {

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(null)
                );

        assertEquals(
                "Inventory request cannot be null.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldThrow_WhenVendorIdMissing() {

        InventoryRequest request =
                buildValidRequest();

        request.setVendorId(null);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(request)
                );

        assertEquals(
                "Vendor Id is mandatory.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldThrow_WhenProductCodeMissing() {

        InventoryRequest request =
                buildValidRequest();

        request.setProductCode(null);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(request)
                );

        assertEquals(
                "SKU is mandatory.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldThrow_WhenProductNameMissing() {

        InventoryRequest request =
                buildValidRequest();

        request.setProductName(null);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(request)
                );

        assertEquals(
                "Product Name is mandatory.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldThrow_WhenQuantityIsNull() {

        InventoryRequest request =
                buildValidRequest();

        request.setQuantity(null);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(request)
                );

        assertEquals(
                "Quantity must be zero or greater.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldThrow_WhenQuantityNegative() {

        InventoryRequest request =
                buildValidRequest();

        request.setQuantity(-1);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(request)
                );

        assertEquals(
                "Quantity must be zero or greater.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldThrow_WhenUnitPriceIsNull() {

        InventoryRequest request =
                buildValidRequest();

        request.setUnitPrice(null);

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(request)
                );

        assertEquals(
                "Unit Price cannot be negative.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldThrow_WhenUnitPriceNegative() {

        InventoryRequest request =
                buildValidRequest();

        request.setUnitPrice(
                BigDecimal.valueOf(-1)
        );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> validation.validate(request)
                );

        assertEquals(
                "Unit Price cannot be negative.",
                exception.getMessage()
        );
    }

    @Test
    void validate_ShouldAcceptZeroQuantity() {

        InventoryRequest request =
                buildValidRequest();

        request.setQuantity(0);

        assertDoesNotThrow(
                () -> validation.validate(request)
        );
    }

    @Test
    void validate_ShouldAcceptZeroUnitPrice() {

        InventoryRequest request =
                buildValidRequest();

        request.setUnitPrice(BigDecimal.ZERO);

        assertDoesNotThrow(
                () -> validation.validate(request)
        );
    }
}