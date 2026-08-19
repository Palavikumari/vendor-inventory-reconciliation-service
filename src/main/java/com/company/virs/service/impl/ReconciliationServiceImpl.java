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
import com.company.virs.service.BatchProcessingResult;
import com.company.virs.service.BatchProcessorService;
import com.company.virs.service.NotificationService;
import com.company.virs.service.ReconciliationService;
import com.company.virs.service.ReferenceInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private final ReferenceInventoryService referenceInventoryService;

    @Override
    public List<InventoryResponse> reconcileBatch(
            UUID batchId) {

        log.info(
                "Starting reconciliation for batch : {}",
                batchId);

        BatchExecution batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        try {

            /*
             * Allow reconciliation only for:
             *
             * PENDING
             * or
             * RETRY -> represented by PENDING status
             */
            if (!BatchStatus.PENDING.name()
                    .equals(batch.getStatus())) {

                throw new IllegalStateException(
                        "Batch cannot be reconciled. "
                                + "Current status : "
                                + batch.getStatus());
            }

            /*
             * Mark processing start.
             */
            batch.setStatus(
                    BatchStatus.RUNNING.name());

            batch.setStartTime(
                    LocalDateTime.now());

            batch.setEndTime(null);

            batchRepository.save(batch);

            /*
             * Fetch inventory records.
             */
            List<VendorInventory> vendorInventories =
                    vendorRepository.findByBatchExecution(
                            batch);

            if (vendorInventories.isEmpty()) {

                batch.setTotalRecords(0);

                batch.setProcessedRecords(0);

                batch.setFailedRecords(0);

                batch.setStatus(
                        BatchStatus.COMPLETED.name());

                batch.setEndTime(
                        LocalDateTime.now());

                batchRepository.save(batch);

                log.info(
                        "No inventory records found for batch : {}",
                        batchId);

                return new ArrayList<>();
            }

            /*
             * Parallel batch processing.
             */
            BatchProcessingResult processingResult =
                    batchProcessorService.processBatches(
                            vendorInventories);

            /*
             * Update actual processing statistics.
             */
            batch.setTotalRecords(
                    processingResult.getTotalRecords());

            batch.setProcessedRecords(
                    processingResult.getProcessedRecords());

            batch.setFailedRecords(
                    processingResult.getFailedRecords());

            /*
             * Do not continue reconciliation
             * when processing failed.
             */
            if (processingResult.getFailedRecords() > 0) {

                batch.setStatus(
                        BatchStatus.FAILED.name());

                batch.setEndTime(
                        LocalDateTime.now());

                batchRepository.save(batch);

                throw new IllegalStateException(
                        "Batch processing failed. "
                                + "Failed records : "
                                + processingResult.getFailedRecords());
            }

            List<InventoryResponse> responses =
                    new ArrayList<>();

            /*
             * Reconcile every vendor record.
             */
            for (VendorInventory vendor :
                    vendorInventories) {

                Integer referenceQuantity =
                        referenceInventoryService
                                .getReferenceQuantity(
                                        vendor.getSku())
                                .orElse(null);

                /*
                 * --------------------------------------------------
                 * CASE 1 - REFERENCE INVENTORY MISSING
                 * --------------------------------------------------
                 */
                if (referenceQuantity == null) {

                    vendor.setReconciliationStatus(
                            ReconciliationStatus.MISSING.name());

                    vendor.setQuantityDifference(null);

                    vendor.setRemarks(
                            "Reference inventory not found for SKU : "
                                    + vendor.getSku());

                    vendor.setNotificationStatus(
                            NotificationStatus.PENDING.name());

                    notificationService.sendNotification(
                            vendor);

                } else {

                    /*
                     * --------------------------------------------------
                     * CASE 2 - REFERENCE INVENTORY FOUND
                     * --------------------------------------------------
                     */
                    int difference =
                            vendor.getQuantity()
                                    - referenceQuantity;

                    vendor.setQuantityDifference(
                            difference);

                    /*
                     * MATCHED
                     */
                    if (difference == 0) {

                        vendor.setReconciliationStatus(
                                ReconciliationStatus.MATCHED.name());

                        vendor.setRemarks(
                                "Inventory matched");

                        vendor.setNotificationStatus(
                                NotificationStatus.PENDING.name());

                    } else {

                        /*
                         * MISMATCH
                         */
                        vendor.setReconciliationStatus(
                                ReconciliationStatus.MISMATCH.name());

                        vendor.setRemarks(
                                "Quantity mismatch");

                        vendor.setNotificationStatus(
                                NotificationStatus.PENDING.name());

                        notificationService.sendNotification(
                                vendor);
                    }
                }

                VendorInventory updatedVendor =
                        vendorRepository.save(
                                vendor);

                responses.add(
                        mapper.toInventoryResponse(
                                updatedVendor));
            }

            /*
             * Reconciliation successful.
             */
            batch.setStatus(
                    BatchStatus.COMPLETED.name());

            batch.setEndTime(
                    LocalDateTime.now());

            batchRepository.save(batch);

            log.info(
                    "Reconciliation completed successfully for batch : {}",
                    batchId);

            return responses;

        } catch (Exception ex) {

            /*
             * Do not overwrite an already FAILED state.
             */
            if (!BatchStatus.FAILED.name()
                    .equals(batch.getStatus())) {

                batch.setStatus(
                        BatchStatus.FAILED.name());

                batch.setEndTime(
                        LocalDateTime.now());

                batchRepository.save(batch);
            }

            log.error(
                    "Reconciliation failed for batch : {}",
                    batchId,
                    ex);

            throw ex;
        }
    }
}