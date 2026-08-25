package com.company.virs.repository;

import com.company.virs.entity.VendorInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface VendorInventoryRepository
        extends JpaRepository<VendorInventory, UUID> {

    List<VendorInventory> findByBatchExecutionBatchId(
            UUID batchId
    );
}