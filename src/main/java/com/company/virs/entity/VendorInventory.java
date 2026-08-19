package com.company.virs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendor_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorInventory {

    @Id
    @Column(name = "vendor_inventory_id")
    private UUID vendorInventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BatchExecution batchExecution;

    @Column(
            name = "vendor_id",
            nullable = false,
            length = 100)
    private String vendorId;

    @Column(
            name = "sku",
            nullable = false,
            length = 100)
    private String sku;

    @Column(
            name = "product_name",
            nullable = false,
            length = 255)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    // Reconciliation Result

    @Column(
            name = "reconciliation_status",
            length = 30)
    private String reconciliationStatus;

    @Column(name = "quantity_difference")
    private Integer quantityDifference;

    @Column(
            name = "remarks",
            length = 500)
    private String remarks;

    // Notification Tracking
    @Column(
            name = "notification_status",
            length = 20)
    private String notificationStatus;

    @Column(name = "notification_time")
    private LocalDateTime notificationTime;
}