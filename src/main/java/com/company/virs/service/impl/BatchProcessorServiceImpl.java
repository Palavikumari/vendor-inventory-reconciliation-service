package com.company.virs.service.impl;

import com.company.virs.entity.VendorInventory;
import com.company.virs.service.BatchProcessingResult;
import com.company.virs.service.BatchProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchProcessorServiceImpl
        implements BatchProcessorService {

    private static final int BATCH_SIZE = 500;

    private final ExecutorService executorService;

    @Override
    public BatchProcessingResult processBatches(
            List<VendorInventory> inventories) {

        if (inventories == null ||
                inventories.isEmpty()) {

            return BatchProcessingResult.builder()
                    .totalRecords(0)
                    .processedRecords(0)
                    .failedRecords(0)
                    .build();
        }

        int totalRecords =
                inventories.size();

        List<Future<Integer>> futures =
                new ArrayList<>();

        /*
         * Divide records into chunks of 500.
         */
        for (int i = 0;
             i < inventories.size();
             i += BATCH_SIZE) {

            /*
             * These variables must be final because
             * they are used inside the lambda.
             */
            final int start = i;

            final int end =
                    Math.min(
                            start + BATCH_SIZE,
                            inventories.size());

            /*
             * Create an independent copy of the chunk.
             */
            final List<VendorInventory> chunk =
                    new ArrayList<>(
                            inventories.subList(
                                    start,
                                    end));

            /*
             * Submit chunk for parallel processing.
             */
            Future<Integer> future =
                    executorService.submit(
                            () -> processChunk(
                                    chunk,
                                    start,
                                    end));

            futures.add(future);
        }

        int processedRecords = 0;

        /*
         * Wait for all chunks to complete.
         */
        for (Future<Integer> future :
                futures) {

            try {

                processedRecords +=
                        future.get();

            } catch (Exception ex) {

                log.error(
                        "Batch chunk execution failed.",
                        ex);
            }
        }

        int failedRecords =
                totalRecords -
                        processedRecords;

        log.info(
                "Batch processing completed. " +
                        "Total : {}, Processed : {}, Failed : {}",
                totalRecords,
                processedRecords,
                failedRecords);

        return BatchProcessingResult.builder()
                .totalRecords(totalRecords)
                .processedRecords(processedRecords)
                .failedRecords(failedRecords)
                .build();
    }

    /**
     * Processes one chunk of inventory records.
     */
    private int processChunk(
            List<VendorInventory> chunk,
            int start,
            int end) {

        int processedRecords = 0;

        log.info(
                "Processing chunk. Start : {}, End : {}, Size : {}",
                start,
                end,
                chunk.size());

        for (VendorInventory inventory :
                chunk) {

            try {

                /*
                 * Basic record validation.
                 *
                 * Actual reconciliation is handled
                 * by ReconciliationService.
                 */

                if (inventory == null) {

                    throw new IllegalArgumentException(
                            "Inventory record cannot be null.");
                }

                if (inventory.getSku() == null ||
                        inventory.getSku().isBlank()) {

                    throw new IllegalArgumentException(
                            "SKU cannot be blank.");
                }

                if (inventory.getQuantity() == null ||
                        inventory.getQuantity() < 0) {

                    throw new IllegalArgumentException(
                            "Quantity cannot be negative.");
                }

                processedRecords++;

                log.debug(
                        "Inventory record processed. SKU : {}",
                        inventory.getSku());

            } catch (Exception ex) {

                log.error(
                        "Failed to process inventory SKU : {}",
                        inventory != null
                                ? inventory.getSku()
                                : null,
                        ex);
            }
        }

        log.info(
                "Chunk completed. " +
                        "Start : {}, End : {}, Processed : {}",
                start,
                end,
                processedRecords);

        return processedRecords;
    }
}