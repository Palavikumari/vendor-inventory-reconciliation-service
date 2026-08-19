package com.company.virs.service.impl;

import com.company.virs.dto.request.BatchRequest;
import com.company.virs.dto.response.BatchResponse;
import com.company.virs.dto.response.UploadResponse;
import com.company.virs.entity.BatchExecution;
import com.company.virs.enums.BatchStatus;
import com.company.virs.enums.ExecutionType;
import com.company.virs.exception.DuplicateBatchException;
import com.company.virs.exception.ResourceNotFoundException;
import com.company.virs.mapper.InventoryMapper;
import com.company.virs.repository.BatchExecutionRepository;
import com.company.virs.service.BatchService;
import com.company.virs.validation.BatchValidation;
import lombok.RequiredArgsConstructor;
import com.company.virs.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BatchServiceImpl
        implements BatchService {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final BatchExecutionRepository batchRepository;

    private final InventoryMapper inventoryMapper;

    private final BatchValidation batchValidation;

    /**
     * ---------------------------------------------------------
     * CREATE BATCH
     * ---------------------------------------------------------
     */
    @Override
    public UploadResponse createBatch(
            BatchRequest request) {

        log.info(
                "Creating new batch for file : {}",
                request != null
                        ? request.getFileName()
                        : null);

        /*
         * Validate request before accessing its fields.
         */
        batchValidation.validate(request);

        /*
         * Prevent duplicate batch creation for
         * the same file.
         */
        if (batchRepository.existsByFileName(
                request.getFileName())) {

            throw new DuplicateBatchException(
                    "Batch already exists for file : "
                            + request.getFileName());
        }

        /*
         * Create BatchExecution entity.
         */
        BatchExecution batchExecution =
                inventoryMapper.toBatchEntity(
                        request);

        /*
         * Initial execution values.
         */
        batchExecution.setTotalRecords(0);

        batchExecution.setProcessedRecords(0);

        batchExecution.setFailedRecords(0);

        batchExecution.setBatchSize(
                DEFAULT_BATCH_SIZE);

        batchExecution.setStatus(
                BatchStatus.PENDING.name());

        /*
         * Execution type should be INITIAL
         * for a newly created batch.
         */
        batchExecution.setExecutionType(
                ExecutionType.INITIAL.name());

        /*
         * Start time is not set here because the batch
         * has not started processing yet.
         *
         * It will be set when actual processing begins.
         */

        batchExecution.setEndTime(null);

        batchExecution =
                batchRepository.save(
                        batchExecution);

        log.info(
                "Batch created successfully. Batch Id : {}",
                batchExecution.getBatchId());

        return inventoryMapper.toUploadResponse(
                batchExecution);
    }

    /**
     * ---------------------------------------------------------
     * GET BATCH
     * ---------------------------------------------------------
     */
    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(
            UUID batchId) {

        BatchExecution batchExecution =
                findBatch(batchId);

        return inventoryMapper.toBatchResponse(
                batchExecution);
    }

    /**
     * ---------------------------------------------------------
     * GET BATCH STATUS
     * ---------------------------------------------------------
     */
    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchStatus(
            UUID batchId) {

        BatchExecution batchExecution =
                findBatch(batchId);

        return inventoryMapper.toBatchResponse(
                batchExecution);
    }

    /**
     * ---------------------------------------------------------
     * RETRY BATCH
     * ---------------------------------------------------------
     *
     * Retry is intended for a FAILED batch.
     *
     * Existing batchId is retained so that the same
     * vendor_inventory records remain associated with
     * the batch.
     */
    @Override
    public BatchResponse retryBatch(UUID batchId) {

        BatchExecution batchExecution =
                batchRepository.findById(batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : " + batchId));

        /*
         * Only FAILED batches are eligible for retry.
         */
        if (!BatchStatus.FAILED.name()
                .equals(batchExecution.getStatus())) {

            throw new ValidationException(
                    "Only FAILED batches can be retried. Current status : "
                            + batchExecution.getStatus());
        }

        /*
         * Change execution type to RETRY.
         */
        batchExecution.setExecutionType(
                ExecutionType.RETRY.name());

        /*
         * Reset batch status so it can be processed again.
         */
        batchExecution.setStatus(
                BatchStatus.PENDING.name());

        /*
         * Reset processing counters for the retry.
         */
        batchExecution.setProcessedRecords(0);

        batchExecution.setFailedRecords(0);

        batchExecution.setEndTime(null);

        batchRepository.save(batchExecution);

        log.info(
                "Batch marked for retry. Batch Id : {}",
                batchId);

        return inventoryMapper.toBatchResponse(
                batchExecution);
    }

    /**
     * ---------------------------------------------------------
     * FIND BATCH
     * ---------------------------------------------------------
     *
     * Centralized batch lookup so that all APIs use the
     * same not-found behavior.
     */
    private BatchExecution findBatch(
            UUID batchId) {

        if (batchId == null) {

            throw new ResourceNotFoundException(
                    "Batch Id cannot be null.");
        }

        return batchRepository.findById(
                        batchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Batch not found : "
                                        + batchId));
    }
}