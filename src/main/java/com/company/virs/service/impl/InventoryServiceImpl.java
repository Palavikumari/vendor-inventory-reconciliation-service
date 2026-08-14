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
import com.company.virs.util.DateUtil;
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
                "Starting inventory upload for Batch Id : {}",
                batchId);

        csvValidation.validate(file);

        String uploadFilePath =
                storageService.uploadFile(file);

        log.info(
                "File stored at: {}",
                uploadFilePath);

        BatchExecution batchExecution =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        List<InventoryRequest> requests =
                csvParser.parse(file);

        List<VendorInventory> vendorInventories =
                new ArrayList<>();

        for (InventoryRequest request : requests) {

            VendorInventory vendorInventory =
                    inventoryMapper.toVendorInventoryEntity(
                            request,
                            batchExecution);

            vendorInventory.setNotificationStatus(
                    NotificationStatus.PENDING.name());

            vendorInventories.add(
                    vendorInventory);
        }

        log.info(
                "Records to save: {}",
                vendorInventories.size());

        vendorRepository.saveAll(
                vendorInventories);

        batchExecution.setTotalRecords(
                vendorInventories.size());

        batchExecution.setProcessedRecords(
                vendorInventories.size());

        batchExecution.setFailedRecords(
                0);

        batchExecution.setBatchSize(
                500);

        batchExecution.setStatus(
                BatchStatus.PENDING.name());


        batchRepository.save(
                batchExecution);

        log.info(
                "Successfully uploaded {} inventory records.",
                vendorInventories.size());

        return inventoryMapper.toUploadResponse(
                batchExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByBatch(
            UUID batchId) {

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

        for (VendorInventory inventory : inventories) {

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

        VendorInventory inventory =
                vendorRepository
                        .findByBatchExecution_BatchIdAndSku(
                                batchId,
                                sku)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found for SKU : "
                                                + sku));

        return inventoryMapper.toInventoryResponse(
                inventory);
    }
}