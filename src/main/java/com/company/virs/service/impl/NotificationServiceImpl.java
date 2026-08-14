package com.company.virs.service.impl;

import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.NotificationStatus;
import com.company.virs.notification.NotificationPublisher;
import com.company.virs.repository.VendorInventoryRepository;
import com.company.virs.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl
        implements NotificationService {

    private final VendorInventoryRepository vendorRepository;

    private final NotificationPublisher notificationPublisher;

    @Override
    public void sendNotification(
            VendorInventory vendorInventory) {

        log.info(
                "Sending notification for SKU : {}",
                vendorInventory.getSku());

        try {

            String message =
                    """
                    🚨 Inventory Discrepancy Detected

                    Batch Id: %s

                    SKU: %s

                    Vendor Quantity: %d

                    Quantity Difference: %d

                    Reconciliation Status: %s

                    Timestamp: %s
                    """
                            .formatted(
                                    vendorInventory.getBatchExecution()
                                            .getBatchId(),
                                    vendorInventory.getSku(),
                                    vendorInventory.getQuantity(),
                                    vendorInventory.getQuantityDifference(),
                                    vendorInventory.getReconciliationStatus(),
                                    LocalDateTime.now());

            notificationPublisher.publishNotification(
                    message);

            vendorInventory.setNotificationStatus(
                    NotificationStatus.SENT.name());

            vendorInventory.setNotificationTime(
                    LocalDateTime.now());

            vendorRepository.save(
                    vendorInventory);

            log.info(
                    "Notification sent successfully for SKU : {}",
                    vendorInventory.getSku());

        } catch (Exception ex) {

            log.error(
                    "Failed to send notification for SKU : {}",
                    vendorInventory.getSku(),
                    ex);

            vendorInventory.setNotificationStatus(
                    NotificationStatus.FAILED.name());

            vendorInventory.setNotificationTime(
                    LocalDateTime.now());

            vendorRepository.save(
                    vendorInventory);
        }
    }
}