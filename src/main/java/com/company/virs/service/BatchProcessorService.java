package com.company.virs.service;

import com.company.virs.entity.VendorInventory;

import java.util.List;

public interface BatchProcessorService {

    BatchProcessingResult processBatches(
            List<VendorInventory> inventories);
}