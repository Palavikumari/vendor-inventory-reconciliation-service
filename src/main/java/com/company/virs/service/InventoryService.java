package com.company.virs.service;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.dto.response.InventoryResponse;
import com.company.virs.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

//    InventoryResponse saveInventory(
//            UUID batchId,
//            InventoryRequest request);
    UploadResponse uploadInventory(UUID
                                   batchId,
                                   MultipartFile file);

    List<InventoryResponse> getInventoryByBatch(UUID batchId);

    InventoryResponse getInventoryBySku(
            UUID batchId,
            String sku);
}
