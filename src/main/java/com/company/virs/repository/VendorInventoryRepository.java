package com.company.virs.repository;

import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorInventoryRepository extends JpaRepository<VendorInventory, UUID> {

    List<VendorInventory> findByBatchExecution(BatchExecution batchExecution);

    Optional<VendorInventory> findByBatchExecution_BatchIdAndSku(
            UUID batchId,
            String sku);

    List<VendorInventory> findByVendorId(String vendorId);

    boolean existsBySku(String sku);
}