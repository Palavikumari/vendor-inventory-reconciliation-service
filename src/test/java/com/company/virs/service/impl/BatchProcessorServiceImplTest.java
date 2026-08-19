package com.company.virs.service.impl;

import com.company.virs.entity.VendorInventory;
import com.company.virs.service.BatchProcessingResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class BatchProcessorServiceImplTest {

    private ExecutorService executorService;

    private BatchProcessorServiceImpl service;

    @BeforeEach
    void setUp() {

        executorService =
                Executors.newFixedThreadPool(4);

        service =
                new BatchProcessorServiceImpl(
                        executorService
                );
    }

    @AfterEach
    void tearDown() {

        executorService.shutdown();
    }

    @Test
    void shouldReturnZeroForEmptyInventory() {

        BatchProcessingResult result =
                service.processBatches(
                        List.of()
                );

        assertEquals(
                0,
                result.getTotalRecords()
        );

        assertEquals(
                0,
                result.getProcessedRecords()
        );

        assertEquals(
                0,
                result.getFailedRecords()
        );
    }

    @Test
    void shouldProcessRecordsSuccessfully() {

        List<VendorInventory> inventories =
                createInventories(10);

        BatchProcessingResult result =
                service.processBatches(
                        inventories
                );

        assertEquals(
                10,
                result.getTotalRecords()
        );

        assertEquals(
                10,
                result.getProcessedRecords()
        );

        assertEquals(
                0,
                result.getFailedRecords()
        );
    }

    @Test
    void shouldProcessMoreThanOneBatch() {

        List<VendorInventory> inventories =
                createInventories(501);

        BatchProcessingResult result =
                service.processBatches(
                        inventories
                );

        assertEquals(
                501,
                result.getTotalRecords()
        );

        assertEquals(
                501,
                result.getProcessedRecords()
        );

        assertEquals(
                0,
                result.getFailedRecords()
        );
    }

    private List<VendorInventory> createInventories(
            int count) {

        List<VendorInventory> inventories =
                new ArrayList<>();

        for (int i = 0; i < count; i++) {

            inventories.add(
                    VendorInventory.builder()
                            .sku("SKU-" + i)
                            .quantity(100)
                            .build()
            );
        }

        return inventories;
    }
}