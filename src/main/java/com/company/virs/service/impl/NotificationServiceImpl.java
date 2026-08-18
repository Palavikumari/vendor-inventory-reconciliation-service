package com.company.virs.service.impl;

import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.NotificationStatus;
import com.company.virs.enums.ReconciliationStatus;
import com.company.virs.notification.NotificationPublisher;
import com.company.virs.repository.VendorInventoryRepository;
import com.company.virs.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl
        implements NotificationService {

    private final VendorInventoryRepository vendorRepository;

    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public void sendNotification(
            VendorInventory vendorInventory) {

        if (vendorInventory == null) {

            log.warn(
                    "Notification skipped because inventory is null.");

            return;
        }

        String status =
                vendorInventory.getReconciliationStatus();

        /*
         * Notification is only required for:
         *
         * MISMATCH
         * MISSING
         */
        if (!ReconciliationStatus.MISMATCH.name()
                .equals(status)
                &&
                !ReconciliationStatus.MISSING.name()
                        .equals(status)) {

            log.debug(
                    "Notification not required for SKU : {}. Status : {}",
                    vendorInventory.getSku(),
                    status);

            return;
        }

        log.info(
                "Sending notification for SKU : {}",
                vendorInventory.getSku());

        try {

            String quantityDifference =
                    vendorInventory.getQuantityDifference() == null
                            ? "N/A"
                            : String.valueOf(
                            vendorInventory
                                    .getQuantityDifference());

            LocalDateTime notificationTime =
                    LocalDateTime.now();

            String message =
                    """
                    🚨 Inventory Discrepancy Detected

                    Batch Id: %s

                    SKU: %s

                    Vendor Quantity: %d

                    Quantity Difference: %s

                    Reconciliation Status: %s

                    Timestamp: %s
                    """
                            .formatted(
                                    vendorInventory
                                            .getBatchExecution()
                                            .getBatchId(),

                                    vendorInventory.getSku(),

                                    vendorInventory.getQuantity(),

                                    quantityDifference,

                                    status,

                                    notificationTime);

            notificationPublisher.publishNotification(
                    message);

            vendorInventory.setNotificationStatus(
                    NotificationStatus.SENT.name());

            vendorInventory.setNotificationTime(
                    notificationTime);

            vendorRepository.save(
                    vendorInventory);

            log.info(
                    "Notification sent successfully for SKU : {}",
                    vendorInventory.getSku());

        } catch (Exception ex) {

            log.error(
                    "Notification failed for SKU : {}",
                    vendorInventory.getSku(),
                    ex);

            vendorInventory.setNotificationStatus(
                    NotificationStatus.FAILED.name());

            vendorInventory.setNotificationTime(
                    LocalDateTime.now());

            try {

                vendorRepository.save(
                        vendorInventory);

            } catch (Exception saveException) {

                log.error(
                        "Unable to save notification failure for SKU : {}",
                        vendorInventory.getSku(),
                        saveException);
            }
        }
    }
}