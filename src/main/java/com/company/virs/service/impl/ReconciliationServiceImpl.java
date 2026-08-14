package com.company.virs.service.impl;

import com.company.virs.dto.response.InventoryResponse;
import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.BatchStatus;
import com.company.virs.enums.NotificationStatus;
import com.company.virs.enums.ReconciliationStatus;
import com.company.virs.exception.ResourceNotFoundException;
import com.company.virs.mapper.InventoryMapper;
import com.company.virs.repository.BatchExecutionRepository;
import com.company.virs.repository.VendorInventoryRepository;
import com.company.virs.service.BatchProcessorService;
import com.company.virs.service.NotificationService;
import com.company.virs.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReconciliationServiceImpl
        implements ReconciliationService {

    private final BatchExecutionRepository batchRepository;

    private final VendorInventoryRepository vendorRepository;

    private final InventoryMapper mapper;

    private final NotificationService notificationService;

    private final BatchProcessorService batchProcessorService;

    /**
     * Temporary external reference data source.
     *
     * Later this will be replaced by:
     * - Vendor API
     * - Master Data Service
     * - External Inventory Service
     */
    private final Map<String, Integer> inventoryReference =
            Map.of(
                    "SKU100", 100,
                    "SKU200", 100,
                    "SKU400", 50,
                    "SKU500", 200
            );

    @Override
    public List<InventoryResponse> reconcileBatch(
            UUID batchId) {

        log.info(
                "Starting reconciliation for batch {}",
                batchId);

        BatchExecution batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        try {

            batch.setStatus(
                    BatchStatus.RUNNING.name());

            batchRepository.save(batch);

            List<VendorInventory> vendorInventories =
                    vendorRepository.findByBatchExecution(
                            batch);

            batchProcessorService.processBatches(
                    vendorInventories);

            List<InventoryResponse> responses =
                    new ArrayList<>();

            for (VendorInventory vendor : vendorInventories) {

                Integer referenceQuantity =
                        inventoryReference.get(
                                vendor.getSku());

                if (referenceQuantity == null) {

                    vendor.setReconciliationStatus(
                            ReconciliationStatus.MISSING.name());

                    vendor.setQuantityDifference(
                            vendor.getQuantity());

                    vendor.setRemarks(
                            "Reference inventory not found for SKU : "
                                    + vendor.getSku());

                    notificationService.sendNotification(
                            vendor);

                    VendorInventory updatedVendor =
                            vendorRepository.save(vendor);

                    responses.add(
                            mapper.toInventoryResponse(
                                    updatedVendor));

                    continue;
                }

                int difference =
                        vendor.getQuantity()
                                - referenceQuantity;

                vendor.setQuantityDifference(
                        difference);

                if (difference == 0) {

                    vendor.setReconciliationStatus(
                            ReconciliationStatus.MATCHED.name());

                    vendor.setRemarks(
                            "Inventory matched");

                    vendor.setNotificationStatus(
                            NotificationStatus.PENDING.name());

                } else {

                    vendor.setReconciliationStatus(
                            ReconciliationStatus.MISMATCH.name());

                    vendor.setRemarks(
                            "Quantity mismatch");

                    notificationService.sendNotification(
                            vendor);
                }

                VendorInventory updatedVendor =
                        vendorRepository.save(
                                vendor);

                responses.add(
                        mapper.toInventoryResponse(
                                updatedVendor));
            }

            batch.setStatus(
                    BatchStatus.COMPLETED.name());

            batch.setEndTime(
                    LocalDateTime.now());

            batchRepository.save(batch);

            log.info(
                    "Reconciliation completed successfully for batch {}",
                    batchId);

            return responses;

        } catch (Exception ex) {

            batch.setStatus(
                    BatchStatus.FAILED.name());

            batch.setEndTime(
                    LocalDateTime.now());

            batchRepository.save(batch);

            log.error(
                    "Reconciliation failed for batch {}",
                    batchId,
                    ex);

            throw ex;
        }

}
}
