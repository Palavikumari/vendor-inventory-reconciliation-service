package com.company.virs.validation;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.exception.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Component
public class InventoryValidation {

    public void validate(InventoryRequest request) {

        if (request == null) {
            throw new ValidationException("Inventory request cannot be null.");
        }

        if (!StringUtils.hasText(request.getVendorId())) {
            throw new ValidationException("Vendor Id is mandatory.");
        }

        if (!StringUtils.hasText(request.getSku())) {
            throw new ValidationException("SKU is mandatory.");
        }

        if (!StringUtils.hasText(request.getProductName())) {
            throw new ValidationException("Product Name is mandatory.");
        }

        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new ValidationException("Quantity must be zero or greater.");
        }

        BigDecimal unitPrice = request.getUnitPrice();

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Unit Price cannot be negative.");
        }
    }
}