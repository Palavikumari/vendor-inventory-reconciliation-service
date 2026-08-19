package com.company.virs.mapper;

import com.company.virs.dto.request.BatchRequest;
import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.dto.response.BatchResponse;
import com.company.virs.dto.response.InventoryResponse;
import com.company.virs.dto.response.UploadResponse;
import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.BatchStatus;
import com.company.virs.enums.NotificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InventoryMapper {

    public BatchExecution toBatchEntity(
            BatchRequest request) {

        return BatchExecution.builder()
                .batchId(UUID.randomUUID())
                .fileName(request.getFileName())
                .executionType(request.getExecutionType())
                .status(BatchStatus.PENDING.name())
                .totalRecords(0)
                .processedRecords(0)
                .failedRecords(0)
                .batchSize(500)
                .startTime(LocalDateTime.now())
                .build();
    }

    public VendorInventory toVendorInventoryEntity(
            InventoryRequest request,
            BatchExecution batchExecution) {

        return VendorInventory.builder()
                .vendorInventoryId(UUID.randomUUID())
                .batchExecution(batchExecution)
                .vendorId(request.getVendorId())
                .sku(request.getSku())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .notificationStatus(
                        NotificationStatus.PENDING.name())
                .uploadTime(LocalDateTime.now())
                .build();
    }

    public BatchResponse toBatchResponse(
            BatchExecution batchExecution) {

        return BatchResponse.builder()
                .batchId(batchExecution.getBatchId())
                .fileName(batchExecution.getFileName())
                .executionType(batchExecution.getExecutionType())
                .status(batchExecution.getStatus())
                .totalRecords(batchExecution.getTotalRecords())
                .processedRecords(batchExecution.getProcessedRecords())
                .failedRecords(batchExecution.getFailedRecords())
                .batchSize(batchExecution.getBatchSize())
                .startTime(batchExecution.getStartTime())
                .endTime(batchExecution.getEndTime())
                .build();
    }

    public InventoryResponse toInventoryResponse(
            VendorInventory vendorInventory) {

        return InventoryResponse.builder()
                .vendorId(vendorInventory.getVendorId())
                .sku(vendorInventory.getSku())
                .productName(vendorInventory.getProductName())
                .vendorQuantity(vendorInventory.getQuantity())
                .quantityDifference(
                        vendorInventory.getQuantityDifference())
                .unitPrice(vendorInventory.getUnitPrice())
                .reconciliationStatus(
                        vendorInventory.getReconciliationStatus())
                .remarks(
                        vendorInventory.getRemarks())
                .notificationStatus(
                        vendorInventory.getNotificationStatus())
                .notificationTime(
                        vendorInventory.getNotificationTime())
                .build();
    }

    public UploadResponse toUploadResponse(
            BatchExecution batchExecution) {

        return UploadResponse.builder()
                .batchId(batchExecution.getBatchId())
                .fileName(batchExecution.getFileName())
                .status(batchExecution.getStatus())
                .message(
                        "Vendor inventory uploaded successfully.")
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}