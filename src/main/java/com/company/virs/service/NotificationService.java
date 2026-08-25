package com.company.virs.service;

import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.NotificationStatus;
import com.company.virs.enums.ReconciliationStatus;
import com.company.virs.notification.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationPublisher notificationPublisher;

    public void notifyDiscrepancies(
            List<VendorInventory> inventories) {

        if (inventories == null || inventories.isEmpty()) {
            return;
        }

        for (VendorInventory inventory : inventories) {

            if (!isDiscrepancy(inventory)) {
                continue;
            }

            send(inventory);
        }
    }

    private boolean isDiscrepancy(
            VendorInventory inventory) {

        if (inventory == null) {
            return false;
        }

        return ReconciliationStatus.MISMATCH.name()
                .equals(inventory.getReconciliationStatus())
                ||
                ReconciliationStatus.MISSING.name()
                        .equals(inventory.getReconciliationStatus());
    }

    private void send(
            VendorInventory inventory) {

        LocalDateTime now =
                LocalDateTime.now();

        try {

            String difference =
                    inventory.getQuantityDifference() == null
                            ? "N/A"
                            : String.valueOf(
                            inventory.getQuantityDifference()
                    );

            String message =
                    """
                    Inventory Discrepancy Detected

                    Batch ID: %s
                    ProductCode: %s
                    Vendor Quantity: %d
                    Quantity Difference: %s
                    Reconciliation Status: %s
                    Timestamp: %s
                    """.formatted(
                            inventory.getBatchExecution().getBatchId(),
                            inventory.getProductCode(),
                            inventory.getQuantity(),
                            difference,
                            inventory.getReconciliationStatus(),
                            now
                    );

            boolean sent =
                    notificationPublisher
                            .publishNotification(message);

            inventory.setNotificationStatus(
                    sent
                            ? NotificationStatus.SENT.name()
                            : NotificationStatus.FAILED.name()
            );

            inventory.setNotificationTime(now);

        } catch (Exception ex) {

            log.error(
                    "Notification processing failed for ProductCode {}",
                    inventory.getProductCode(),
                    ex
            );

            inventory.setNotificationStatus(
                    NotificationStatus.FAILED.name()
            );

            inventory.setNotificationTime(now);
        }
    }
}