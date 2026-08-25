package com.company.virs.service;

import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.ReconciliationStatus;
import com.company.virs.notification.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private NotificationService service;

    private BatchExecution batch;

    @BeforeEach
    void setUp() {

        batch = BatchExecution.builder()
                .batchId(UUID.randomUUID())
                .fileName("inventory.csv")
                .startTime(LocalDateTime.now())
                .batchSize(10)
                .status("COMPLETED")
                .executionType("INITIAL")
                .build();
    }

    private VendorInventory mismatchInventory() {

        return VendorInventory.builder()
                .vendorInventoryId(UUID.randomUUID())
                .batchExecution(batch)
                .vendorId("V001")
                .productCode("P001")
                .productName("Laptop")
                .quantity(10)
                .quantityDifference(5)
                .reconciliationStatus(
                        ReconciliationStatus.MISMATCH.name()
                )
                .uploadTime(LocalDateTime.now())
                .build();
    }

    private VendorInventory matchedInventory() {

        return VendorInventory.builder()
                .vendorInventoryId(UUID.randomUUID())
                .batchExecution(batch)
                .vendorId("V001")
                .productCode("P001")
                .productName("Laptop")
                .quantity(10)
                .reconciliationStatus(
                        ReconciliationStatus.MATCHED.name()
                )
                .uploadTime(LocalDateTime.now())
                .build();
    }

    @Test
    void notifyDiscrepancies_ShouldReturn_WhenListIsNull() {

        assertDoesNotThrow(
                () -> service.notifyDiscrepancies(null)
        );

        verifyNoInteractions(
                notificationPublisher
        );
    }

    @Test
    void notifyDiscrepancies_ShouldReturn_WhenListEmpty() {

        service.notifyDiscrepancies(
                List.of()
        );

        verifyNoInteractions(
                notificationPublisher
        );
    }

    @Test
    void notifyDiscrepancies_ShouldIgnoreMatchedRecords() {

        VendorInventory inventory =
                matchedInventory();

        service.notifyDiscrepancies(
                List.of(inventory)
        );

        verifyNoInteractions(
                notificationPublisher
        );

        assertNull(
                inventory.getNotificationStatus()
        );
    }

    @Test
    void notifyDiscrepancies_ShouldSendMismatchSuccessfully() {

        VendorInventory inventory =
                mismatchInventory();

        when(
                notificationPublisher
                        .publishNotification(anyString())
        ).thenReturn(true);

        service.notifyDiscrepancies(
                List.of(inventory)
        );

        verify(notificationPublisher)
                .publishNotification(anyString());

        assertEquals(
                "SENT",
                inventory.getNotificationStatus()
        );

        assertNotNull(
                inventory.getNotificationTime()
        );
    }

    @Test
    void notifyDiscrepancies_ShouldMarkFailed_WhenNotificationReturnsFalse() {

        VendorInventory inventory =
                mismatchInventory();

        when(
                notificationPublisher
                        .publishNotification(anyString())
        ).thenReturn(false);

        service.notifyDiscrepancies(
                List.of(inventory)
        );

        assertEquals(
                "FAILED",
                inventory.getNotificationStatus()
        );

        assertNotNull(
                inventory.getNotificationTime()
        );
    }

    @Test
    void notifyDiscrepancies_ShouldMarkFailed_WhenPublisherThrowsException() {

        VendorInventory inventory =
                mismatchInventory();

        when(
                notificationPublisher
                        .publishNotification(anyString())
        ).thenThrow(
                new RuntimeException("error")
        );

        assertDoesNotThrow(
                () -> service.notifyDiscrepancies(
                        List.of(inventory)
                )
        );

        assertEquals(
                "FAILED",
                inventory.getNotificationStatus()
        );

        assertNotNull(
                inventory.getNotificationTime()
        );
    }

    @Test
    void notifyDiscrepancies_ShouldHandleMissingStatus() {

        VendorInventory inventory =
                mismatchInventory();

        inventory.setReconciliationStatus(
                ReconciliationStatus.MISSING.name()
        );

        when(
                notificationPublisher
                        .publishNotification(anyString())
        ).thenReturn(true);

        service.notifyDiscrepancies(
                List.of(inventory)
        );

        assertEquals(
                "SENT",
                inventory.getNotificationStatus()
        );
    }

    @Test
    void notifyDiscrepancies_ShouldHandleNullInventory() {

        List<VendorInventory> inventories =
                new java.util.ArrayList<>();

        inventories.add(null);

        assertDoesNotThrow(
                () -> service.notifyDiscrepancies(inventories)
        );

        verifyNoInteractions(
                notificationPublisher
        );
    }
}