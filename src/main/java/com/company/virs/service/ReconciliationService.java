package com.company.virs.service;

import com.company.virs.dto.response.InventoryResponse;

import java.util.List;
import java.util.UUID;

public interface ReconciliationService {

    List<InventoryResponse> reconcileBatch(UUID batchId);
}