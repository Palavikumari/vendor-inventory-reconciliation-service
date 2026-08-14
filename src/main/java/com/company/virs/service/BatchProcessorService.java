package com.company.virs.service;

import com.company.virs.entity.VendorInventory;

import java.util.List;

public interface BatchProcessorService {

    void processBatches(
            List<VendorInventory> inventories);
}