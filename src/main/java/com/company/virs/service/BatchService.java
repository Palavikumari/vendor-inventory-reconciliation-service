package com.company.virs.service;

import com.company.virs.dto.request.BatchRequest;
import com.company.virs.dto.response.BatchResponse;
import com.company.virs.dto.response.UploadResponse;

import java.util.UUID;

public interface BatchService {

    BatchResponse retryBatch(UUID batchId);

    UploadResponse createBatch(BatchRequest request);

    BatchResponse getBatchById(UUID batchId);

    BatchResponse getBatchStatus(UUID batchId);
}
