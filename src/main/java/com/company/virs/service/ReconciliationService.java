package com.company.virs.service;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.dto.response.ReconciliationResponse;
import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import com.company.virs.enums.BatchStatus;
import com.company.virs.enums.ExecutionType;
import com.company.virs.enums.ReconciliationStatus;
import com.company.virs.exception.ResourceNotFoundException;
import com.company.virs.exception.ValidationException;
import com.company.virs.parser.CsvParser;
import com.company.virs.repository.BatchExecutionRepository;
import com.company.virs.repository.InternalInventoryRepository;
import com.company.virs.repository.VendorInventoryRepository;
import com.company.virs.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private static final long DEFAULT_MAX_FILE_SIZE =
            10L * 1024 * 1024;

    private final BatchExecutionRepository batchRepository;

    private final VendorInventoryRepository vendorRepository;

    private final InternalInventoryRepository internalInventoryRepository;

    private final CsvParser csvParser;

    private final StorageService storageService;

    private final NotificationService notificationService;

    private final ExecutorService executorService;

    @Value("${virs.processing.batch-size:500}")
    private int batchSize;

    @Value("${virs.processing.max-file-size-bytes:10485760}")
    private long maxFileSize;

    @Transactional
    public ReconciliationResponse reconcile(
            MultipartFile file) {

        validateFile(file);

        String fileName =
                Objects.requireNonNull(
                        file.getOriginalFilename()
                ).trim();

        BatchExecution batch =
                createBatch(
                        fileName,
                        ExecutionType.INITIAL,
                        null
                );

        String objectName =
                batch.getBatchId()
                        + "/"
                        + sanitizeFileName(fileName);

        try {

            storageService.uploadFile(
                    file,
                    objectName
            );

            List<InventoryRequest> requests =
                    csvParser.parse(file);

            List<VendorInventory> inventories =
                    createInventoryRecords(
                            requests,
                            batch
                    );

            vendorRepository.saveAll(
                    inventories
            );

            batch.setTotalRecords(
                    inventories.size()
            );

            batchRepository.save(batch);

            processAndFinalize(
                    batch,
                    inventories
            );

            return buildResponse(
                    batch,
                    inventories
            );

        } catch (Exception ex) {

            markBatchFailed(
                    batch,
                    ex
            );

            throw ex;
        }
    }

    public ReconciliationResponse getReconciliationResult(
            UUID batchId) {

        BatchExecution batch =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found: "
                                                + batchId
                                )
                        );

        List<VendorInventory> inventories =
                vendorRepository.findByBatchExecutionBatchId(
                        batchId
                );

        return buildResponse(
                batch,
                inventories
        );
    }

    @Transactional
    public ReconciliationResponse retrigger(
            UUID sourceBatchId) {

        BatchExecution sourceBatch =
                batchRepository.findById(sourceBatchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found: "
                                                + sourceBatchId
                                )
                        );

        List<VendorInventory> sourceRecords =
                vendorRepository.findByBatchExecutionBatchId(
                        sourceBatchId
                );

        if (sourceRecords.isEmpty()) {

            throw new ValidationException(
                    "No inventory records found for batch: "
                            + sourceBatchId
            );
        }

        BatchExecution retryBatch =
                createBatch(
                        sourceBatch.getFileName(),
                        ExecutionType.RETRIGGER,
                        sourceBatchId
                );

        try {

            List<VendorInventory> retryRecords =
                    copyRecords(
                            sourceRecords,
                            retryBatch
                    );

            vendorRepository.saveAll(
                    retryRecords
            );

            retryBatch.setTotalRecords(
                    retryRecords.size()
            );

            batchRepository.save(
                    retryBatch
            );

            processAndFinalize(
                    retryBatch,
                    retryRecords
            );

            return buildResponse(
                    retryBatch,
                    retryRecords
            );

        } catch (DataIntegrityViolationException ex) {

            markBatchFailed(
                    retryBatch,
                    ex
            );

            log.error(
                    "Database error while retriggering source batch {}",
                    sourceBatchId,
                    ex
            );

            throw ex;

        } catch (Exception ex) {

            markBatchFailed(
                    retryBatch,
                    ex
            );

            throw ex;
        }
    }

    private void processAndFinalize(
            BatchExecution batch,
            List<VendorInventory> inventories) {

        Map<String, Integer> references =
                loadReferenceQuantities(
                        inventories
                );

        ProcessingResult processingResult =
                processRecords(
                        inventories,
                        references
                );

        applyOutcomes(
                inventories,
                processingResult.outcomes()
        );

        vendorRepository.saveAll(
                inventories
        );

        notificationService.notifyDiscrepancies(
                inventories
        );

        vendorRepository.saveAll(
                inventories
        );

        batch.setProcessedRecords(
                processingResult.processedCount()
        );

        batch.setFailedRecords(
                processingResult.failedCount()
        );

        batch.setStatus(
                processingResult.failedCount() == 0
                        ? BatchStatus.COMPLETED.name()
                        : BatchStatus.FAILED.name()
        );

        batch.setEndTime(
                LocalDateTime.now()
        );

        batchRepository.save(
                batch
        );
    }

    private BatchExecution createBatch(
            String fileName,
            ExecutionType executionType,
            UUID sourceBatchId) {

        LocalDateTime now =
                LocalDateTime.now();

        BatchExecution batch =
                BatchExecution.builder()
                        .batchId(UUID.randomUUID())
                        .fileName(fileName)
                        .executionType(executionType.name())
                        .sourceBatchId(sourceBatchId)
                        .status(BatchStatus.RUNNING.name())
                        .totalRecords(0)
                        .processedRecords(0)
                        .failedRecords(0)
                        .batchSize(batchSize)
                        .startTime(now)
                        .build();

        return batchRepository.save(batch);
    }

    private List<VendorInventory> createInventoryRecords(
            List<InventoryRequest> requests,
            BatchExecution batch) {

        LocalDateTime now =
                LocalDateTime.now();

        return requests.stream()
                .map(request ->
                        VendorInventory.builder()
                                .vendorInventoryId(
                                        UUID.randomUUID()
                                )
                                .batchExecution(batch)
                                .vendorId(
                                        request.getVendorId().trim()
                                )
                                .productCode(
                                        request.getProductCode().trim()
                                )
                                .productName(
                                        request.getProductName().trim()
                                )
                                .quantity(
                                        request.getQuantity()
                                )
                                .unitPrice(
                                        request.getUnitPrice()
                                )
                                .uploadTime(now)
                                .reconciliationStatus(null)
                                .quantityDifference(null)
                                .remarks(null)
                                .notificationStatus(null)
                                .notificationTime(null)
                                .build()
                )
                .toList();
    }


    private List<VendorInventory> copyRecords(
            List<VendorInventory> sourceRecords,
            BatchExecution batch) {

        LocalDateTime now =
                LocalDateTime.now();

        return sourceRecords.stream()
                .map(source ->
                        VendorInventory.builder()
                                .vendorInventoryId(
                                        UUID.randomUUID()
                                )
                                .batchExecution(batch)
                                .vendorId(
                                        source.getVendorId()
                                )
                                .productCode(
                                        source.getProductCode()
                                )
                                .productName(
                                        source.getProductName()
                                )
                                .quantity(
                                        source.getQuantity()
                                )
                                .unitPrice(
                                        source.getUnitPrice()
                                )
                                .uploadTime(now)
                                .reconciliationStatus(null)
                                .quantityDifference(null)
                                .remarks(null)
                                .notificationStatus(null)
                                .notificationTime(null)
                                .build()
                )
                .toList();
    }


    private Map<String, Integer> loadReferenceQuantities(
            List<VendorInventory> inventories) {

        if (inventories == null ||
                inventories.isEmpty()) {

            return Collections.emptyMap();
        }

        List<String> productCodes =
                inventories.stream()
                        .filter(Objects::nonNull)
                        .map(VendorInventory::getProductCode)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(productCode ->
                                !productCode.isBlank()
                        )
                        .distinct()
                        .toList();

        if (productCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        return internalInventoryRepository
                .findQuantitiesByProductCode(
                        productCodes
                );
    }

    private ProcessingResult processRecords(
            List<VendorInventory> inventories,
            Map<String, Integer> referenceQuantities) {

        if (inventories == null ||
                inventories.isEmpty()) {

            return new ProcessingResult(
                    List.of(),
                    0,
                    0
            );
        }

        Map<String, Integer> safeReferences =
                referenceQuantities == null
                        ? Collections.emptyMap()
                        : referenceQuantities;

        List<Future<List<Outcome>>> futures =
                new ArrayList<>();

        for (int start = 0;
             start < inventories.size();
             start += batchSize) {

            int end =
                    Math.min(
                            start + batchSize,
                            inventories.size()
                    );

            List<VendorInventory> chunk =
                    new ArrayList<>(
                            inventories.subList(
                                    start,
                                    end
                            )
                    );

            futures.add(
                    executorService.submit(
                            () ->
                                    reconcileChunk(
                                            chunk,
                                            safeReferences
                                    )
                    )
            );
        }

        List<Outcome> outcomes =
                new ArrayList<>(
                        inventories.size()
                );

        for (Future<List<Outcome>> future :
                futures) {

            try {

                List<Outcome> chunkOutcomes =
                        future.get();

                if (chunkOutcomes != null) {
                    outcomes.addAll(chunkOutcomes);
                }

            } catch (InterruptedException ex) {

                Thread.currentThread()
                        .interrupt();

                log.error(
                        "Batch processing interrupted",
                        ex
                );

                break;

            } catch (ExecutionException ex) {

                log.error(
                        "Batch chunk processing failed",
                        ex
                );

                break;
            }
        }

        Set<UUID> processedIds =
                outcomes.stream()
                        .map(Outcome::inventoryId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        int failedCount =
                (int) inventories.stream()
                        .filter(Objects::nonNull)
                        .map(
                                VendorInventory::getVendorInventoryId
                        )
                        .filter(
                                id -> !processedIds.contains(id)
                        )
                        .count();

        return new ProcessingResult(
                outcomes,
                inventories.size() - failedCount,
                failedCount
        );
    }


    private List<Outcome> reconcileChunk(
            List<VendorInventory> chunk,
            Map<String, Integer> references) {

        if (chunk == null ||
                chunk.isEmpty()) {

            return List.of();
        }

        Map<String, Integer> safeReferences =
                references == null
                        ? Collections.emptyMap()
                        : references;

        List<Outcome> outcomes =
                new ArrayList<>(
                        chunk.size()
                );

        for (VendorInventory inventory :
                chunk) {

            try {

                if (inventory == null ||
                        inventory.getVendorInventoryId() == null ||
                        inventory.getProductCode() == null ||
                        inventory.getQuantity() == null) {

                    outcomes.add(
                            failedOutcome(
                                    inventory,
                                    "Invalid inventory record."
                            )
                    );

                    continue;
                }

                String productCode =
                        inventory.getProductCode().trim();

                if (productCode.isBlank()) {

                    outcomes.add(
                            failedOutcome(
                                    inventory,
                                    "Product code cannot be blank."
                            )
                    );

                    continue;
                }

                Integer referenceQuantity =
                        safeReferences.get(productCode);

                if (referenceQuantity == null) {

                    outcomes.add(
                            new Outcome(
                                    inventory.getVendorInventoryId(),
                                    ReconciliationStatus.MISSING.name(),
                                    null,
                                    "Reference inventory not found for product code: "
                                            + productCode
                            )
                    );

                    continue;
                }

                int difference =
                        inventory.getQuantity()
                                - referenceQuantity;

                if (difference == 0) {

                    outcomes.add(
                            new Outcome(
                                    inventory.getVendorInventoryId(),
                                    ReconciliationStatus.MATCHED.name(),
                                    0,
                                    "Inventory matched"
                            )
                    );

                } else {

                    outcomes.add(
                            new Outcome(
                                    inventory.getVendorInventoryId(),
                                    ReconciliationStatus.MISMATCH.name(),
                                    difference,
                                    "Quantity mismatch"
                            )
                    );
                }

            } catch (Exception ex) {

                log.error(
                        "Unable to reconcile product code {}",
                        inventory != null
                                ? inventory.getProductCode()
                                : null,
                        ex
                );

                outcomes.add(
                        new Outcome(
                                inventory != null
                                        ? inventory.getVendorInventoryId()
                                        : null,
                                ReconciliationStatus.FAILED.name(),
                                null,
                                "Reconciliation processing failed: "
                                        + ex.getMessage()
                        )
                );
            }
        }

        return outcomes;
    }

    private Outcome failedOutcome(
            VendorInventory inventory,
            String remarks) {

        return new Outcome(
                inventory != null
                        ? inventory.getVendorInventoryId()
                        : null,
                ReconciliationStatus.FAILED.name(),
                null,
                remarks
        );
    }

    private void applyOutcomes(
            List<VendorInventory> inventories,
            List<Outcome> outcomes) {

        if (inventories == null ||
                inventories.isEmpty()) {
            return;
        }

        Map<UUID, Outcome> outcomeMap =
                outcomes == null
                        ? Collections.emptyMap()
                        : outcomes.stream()
                        .filter(
                                outcome ->
                                        outcome != null &&
                                                outcome.inventoryId() != null
                        )
                        .collect(
                                Collectors.toMap(
                                        Outcome::inventoryId,
                                        outcome -> outcome,
                                        (first, second) -> first
                                )
                        );

        for (VendorInventory inventory :
                inventories) {

            if (inventory == null) {
                continue;
            }

            Outcome outcome =
                    outcomeMap.get(
                            inventory.getVendorInventoryId()
                    );

            if (outcome == null) {

                inventory.setReconciliationStatus(
                        ReconciliationStatus.FAILED.name()
                );

                inventory.setQuantityDifference(null);

                inventory.setRemarks(
                        "No reconciliation outcome generated."
                );

                continue;
            }

            inventory.setReconciliationStatus(
                    outcome.status()
            );

            inventory.setQuantityDifference(
                    outcome.difference()
            );

            inventory.setRemarks(
                    outcome.remarks()
            );
        }
    }

    private void validateFile(
            MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

            throw new ValidationException(
                    "CSV file cannot be empty."
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null ||
                fileName.isBlank() ||
                !fileName
                        .toLowerCase(Locale.ROOT)
                        .endsWith(".csv")) {

            throw new ValidationException(
                    "Only CSV files are supported."
            );
        }

        long configuredLimit =
                maxFileSize > 0
                        ? maxFileSize
                        : DEFAULT_MAX_FILE_SIZE;

        if (file.getSize() > configuredLimit) {

            throw new ValidationException(
                    "CSV file size cannot exceed "
                            + configuredLimit
                            + " bytes."
            );
        }

        if (batchSize <= 0) {

            throw new IllegalStateException(
                    "Batch size must be greater than zero."
            );
        }
    }

    private String sanitizeFileName(
            String fileName) {

        String sanitized =
                fileName.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        return sanitized.isBlank()
                ? "inventory.csv"
                : sanitized;
    }

    private void markBatchFailed(
            BatchExecution batch,
            Exception ex) {

        if (batch == null) {
            log.error(
                    "Reconciliation failed because batch is null",
                    ex
            );
            return;
        }

        batch.setStatus(
                BatchStatus.FAILED.name()
        );

        batch.setEndTime(
                LocalDateTime.now()
        );

        batchRepository.save(
                batch
        );

        log.error(
                "Reconciliation failed for batch {}",
                batch.getBatchId(),
                ex
        );
    }


    private ReconciliationResponse buildResponse(
            BatchExecution batch,
            List<VendorInventory> inventories) {

        List<VendorInventory> safeInventories =
                inventories == null
                        ? Collections.emptyList()
                        : inventories;

        int discrepancies =
                (int) safeInventories.stream()
                        .filter(Objects::nonNull)
                        .filter(this::isDiscrepancy)
                        .count();

        return ReconciliationResponse.builder()
                .batchId(batch.getBatchId())
                .fileName(batch.getFileName())
                .executionType(batch.getExecutionType())
                .status(batch.getStatus())
                .totalRecords(batch.getTotalRecords())
                .processedRecords(batch.getProcessedRecords())
                .failedRecords(batch.getFailedRecords())
                .batchSize(batch.getBatchSize())
                .discrepancyRecords(discrepancies)
                .startTime(batch.getStartTime())
                .endTime(batch.getEndTime())
                .notificationStatus(
                        determineNotificationStatus(
                                safeInventories
                        )
                )
                .build();
    }

    private String determineNotificationStatus(
            List<VendorInventory> inventories) {

        if (inventories == null ||
                inventories.isEmpty()) {

            return "SENT";
        }

        boolean hasDiscrepancy =
                inventories.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(this::isDiscrepancy);

        if (!hasDiscrepancy) {
            return "SENT";
        }

        boolean allNotificationsSent =
                inventories.stream()
                        .filter(Objects::nonNull)
                        .filter(this::isDiscrepancy)
                        .allMatch(inventory ->
                                "SENT".equals(
                                        inventory.getNotificationStatus()
                                )
                        );

        return allNotificationsSent
                ? "SENT"
                : "FAILED";
    }

    private boolean isDiscrepancy(
            VendorInventory inventory) {

        if (inventory == null) {
            return false;
        }

        return ReconciliationStatus.MISMATCH.name()
                .equals(
                        inventory.getReconciliationStatus()
                )
                ||
                ReconciliationStatus.MISSING.name()
                        .equals(
                                inventory.getReconciliationStatus()
                        );
    }

    private record Outcome(
            UUID inventoryId,
            String status,
            Integer difference,
            String remarks) {
    }

    private record ProcessingResult(
            List<Outcome> outcomes,
            int processedCount,
            int failedCount) {
    }
}