package com.company.virs.service.impl;

import com.company.virs.entity.VendorInventory;
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
    public void processBatches(
            List<VendorInventory> inventories) {

        List<Future<?>> futures =
                new ArrayList<>();

        for (int i = 0;
             i < inventories.size();
             i += BATCH_SIZE) {

            int end =
                    Math.min(
                            i + BATCH_SIZE,
                            inventories.size());

            List<VendorInventory> chunk =
                    inventories.subList(i, end);

            Future<?> future =
                    executorService.submit(() -> {

                        log.info(
                                "Processing chunk size {}",
                                chunk.size());

                        chunk.forEach(
                                inventory -> {
                                    // process record
                                });
                    });

            futures.add(future);
        }

        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}