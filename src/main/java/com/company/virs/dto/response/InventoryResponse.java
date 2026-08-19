package com.company.virs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "InventoryResponse",
        description = "Inventory reconciliation response"
)
public class InventoryResponse {

    @Schema(
            example = "VENDOR001",
            description = "Vendor identifier"
    )
    private String vendorId;

    @Schema(
            example = "SKU100",
            description = "Product SKU"
    )
    private String sku;

    @Schema(
            example = "Laptop",
            description = "Product name"
    )
    private String productName;

    @Schema(
            example = "100",
            description = "Vendor reported quantity"
    )
    private Integer vendorQuantity;

    @Schema(
            example = "50",
            description = "Difference between vendor quantity and reference inventory quantity"
    )
    private Integer quantityDifference;

    @Schema(
            example = "25000.00",
            description = "Unit price provided by vendor"
    )
    private BigDecimal unitPrice;

    @Schema(
            example = "MISMATCH",
            description = """
                    Reconciliation result.

                    Possible values:
                    MATCHED
                    MISMATCH
                    MISSING
                    """
    )
    private String reconciliationStatus;

    @Schema(
            example = "Quantity mismatch",
            description = "Additional reconciliation remarks"
    )
    private String remarks;

    @Schema(
            example = "SENT",
            description = """
                    Notification processing status.

                    Possible values:
                    PENDING
                    SENT
                    FAILED
                    """
    )
    private String notificationStatus;

    @Schema(
            example = "2026-08-11T14:20:35",
            description = "Notification processing timestamp"
    )
    private LocalDateTime notificationTime;
}