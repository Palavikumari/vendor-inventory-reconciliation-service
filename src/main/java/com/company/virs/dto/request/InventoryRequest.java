package com.company.virs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "InventoryRequest",
        description = "Vendor inventory record"
)
public class InventoryRequest {

    @NotBlank
    @Schema(example = "VENDOR001")
    private String vendorId;

    @NotBlank
    @Schema(example = "SKU1001")
    private String productCode;

    @NotBlank
    @Schema(example = "Laptop")
    private String productName;

    @NotNull
    @PositiveOrZero
    @Schema(example = "150")
    private Integer quantity;

    @NotNull
    @PositiveOrZero
    @Schema(example = "24999.99")
    private BigDecimal unitPrice;
}