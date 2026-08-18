package com.company.virs.service.impl;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.dto.response.InventoryResponse;
import com.company.virs.dto.response.UploadResponse;
import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.BatchStatus;
import com.company.virs.enums.NotificationStatus;
import com.company.virs.exception.ResourceNotFoundException;
import com.company.virs.mapper.InventoryMapper;
import com.company.virs.parser.CsvParser;
import com.company.virs.repository.BatchExecutionRepository;
import com.company.virs.repository.VendorInventoryRepository;
import com.company.virs.service.InventoryService;
import com.company.virs.storage.StorageService;
import com.company.virs.validation.CsvValidation;
import com.company.virs.validation.InventoryValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryServiceImpl
        implements InventoryService {

    private final BatchExecutionRepository batchRepository;

    private final VendorInventoryRepository vendorRepository;

    private final InventoryMapper inventoryMapper;

    private final InventoryValidation inventoryValidation;

    private final CsvValidation csvValidation;

    private final CsvParser csvParser;

    private final StorageService storageService;

    @Override
    public UploadResponse uploadInventory(
            UUID batchId,
            MultipartFile file) {

        log.info(
                "Starting inventory upload for batch : {}",
                batchId);

        /*
         * 1. Validate uploaded file.
         */
        csvValidation.validate(file);

        /*
         * 2. Find batch.
         */
        BatchExecution batchExecution =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        /*
         * 3. Only allow upload when batch is PENDING.
         */
        if (!BatchStatus.PENDING.name()
                .equals(batchExecution.getStatus())) {

            throw new IllegalStateException(
                    "Inventory can only be uploaded for a PENDING batch. "
                            + "Current status : "
                            + batchExecution.getStatus());
        }

        /*
         * 4. Store original CSV in MinIO.
         */
        String storedFileName =
                storageService.uploadFile(file);

        log.info(
                "File stored successfully in MinIO : {}",
                storedFileName);

        /*
         * 5. Parse CSV.
         */
        List<InventoryRequest> requests =
                csvParser.parse(file);

        if (requests == null || requests.isEmpty()) {

            /*
             * Remove uploaded file if CSV contains no records.
             */
            storageService.deleteFile(
                    storedFileName);

            throw new IllegalArgumentException(
                    "CSV file does not contain any inventory records.");
        }

        /*
         * 6. Validate and convert every CSV record.
         */
        List<VendorInventory> vendorInventories =
                new ArrayList<>();

        for (InventoryRequest request : requests) {

            inventoryValidation.validate(request);

            VendorInventory vendorInventory =
                    inventoryMapper.toVendorInventoryEntity(
                            request,
                            batchExecution);

            vendorInventory.setNotificationStatus(
                    NotificationStatus.PENDING.name());

            vendorInventories.add(
                    vendorInventory);
        }

        /*
         * 7. Persist inventory.
         */
        vendorRepository.saveAll(
                vendorInventories);

        /*
         * 8. Update batch statistics.
         *
         * Uploading is not processing.
         */
        batchExecution.setTotalRecords(
                vendorInventories.size());

        batchExecution.setProcessedRecords(0);

        batchExecution.setFailedRecords(0);

        batchExecution.setBatchSize(
                500);

        batchExecution.setStatus(
                BatchStatus.PENDING.name());

        batchRepository.save(
                batchExecution);

        log.info(
                "Inventory upload completed. Batch : {}, Records : {}",
                batchId,
                vendorInventories.size());

        return inventoryMapper.toUploadResponse(
                batchExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByBatch(
            UUID batchId) {

        log.info(
                "Fetching inventory for batch : {}",
                batchId);

        BatchExecution batchExecution =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        List<VendorInventory> inventories =
                vendorRepository.findByBatchExecution(
                        batchExecution);

        List<InventoryResponse> responses =
                new ArrayList<>();

        for (VendorInventory inventory :
                inventories) {

            responses.add(
                    inventoryMapper.toInventoryResponse(
                            inventory));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryBySku(
            UUID batchId,
            String sku) {

        if (sku == null || sku.isBlank()) {

            throw new IllegalArgumentException(
                    "SKU cannot be blank.");
        }

        /*
         * Verify batch exists.
         */
        batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Batch not found : "
                                        + batchId));

        VendorInventory inventory =
                vendorRepository
                        .findByBatchExecution_BatchIdAndSku(
                                batchId,
                                sku)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found for SKU : "
                                                + sku
                                                + " in Batch : "
                                                + batchId));

        return inventoryMapper.toInventoryResponse(
                inventory);
    }
}