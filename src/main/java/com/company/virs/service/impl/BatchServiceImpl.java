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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BatchServiceImpl
        implements BatchService {

    private final BatchExecutionRepository batchRepository;

    private final InventoryMapper inventoryMapper;

    private final BatchValidation batchValidation;

    @Override
    public UploadResponse createBatch(
            BatchRequest request) {

        log.info(
                "Creating new batch for file: {}",
                request.getFileName());

        batchValidation.validate(
                request);

        if (batchRepository.existsByFileName(
                request.getFileName())) {

            throw new DuplicateBatchException(
                    "Batch already exists for file : "
                            + request.getFileName());
        }

        BatchExecution batchExecution =
                inventoryMapper.toBatchEntity(
                        request);

        batchExecution.setTotalRecords(
                0);

        batchExecution.setProcessedRecords(
                0);

        batchExecution.setFailedRecords(
                0);

        batchExecution.setBatchSize(
                500);

        batchExecution.setStatus(
                BatchStatus.PENDING.name());

        batchExecution =
                batchRepository.save(
                        batchExecution);

        log.info(
                "Batch created successfully. Batch Id : {}",
                batchExecution.getBatchId());

        return inventoryMapper.toUploadResponse(
                batchExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(
            UUID batchId) {

        BatchExecution batchExecution =
                batchRepository.findById(
                                batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        return inventoryMapper.toBatchResponse(
                batchExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchStatus(
            UUID batchId) {

        BatchExecution batchExecution =
                batchRepository.findById(
                                batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        return inventoryMapper.toBatchResponse(
                batchExecution);
    }

    @Override
    public BatchResponse retryBatch(
            UUID batchId) {

        BatchExecution batchExecution =
                batchRepository.findById(
                                batchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batch not found : "
                                                + batchId));

        batchExecution.setExecutionType(
                ExecutionType.RETRY.name());

        batchExecution.setStatus(
                BatchStatus.PENDING.name());

        batchRepository.save(
                batchExecution);

        log.info(
                "Batch marked for retry. Batch Id : {}",
                batchId);

        return inventoryMapper.toBatchResponse(
                batchExecution);
    }
}