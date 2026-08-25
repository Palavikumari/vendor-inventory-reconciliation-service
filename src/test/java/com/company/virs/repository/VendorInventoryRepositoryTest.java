package com.company.virs.repository;

import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.flyway.enabled=false"
        }
)

class VendorInventoryRepositoryTest {

    @Autowired
    private VendorInventoryRepository repository;

    @Autowired
    private BatchExecutionRepository batchRepository;

    private BatchExecution createBatch() {

        return batchRepository.save(
                BatchExecution.builder()
                        .batchId(UUID.randomUUID())
                        .fileName("inventory.csv")
                        .executionType("INITIAL")
                        .status("RUNNING")
                        .batchSize(100)
                        .totalRecords(0)
                        .processedRecords(0)
                        .failedRecords(0)
                        .startTime(LocalDateTime.now())
                        .build()
        );
    }

    private VendorInventory createInventory(
            BatchExecution batch,
            String productCode) {

        return VendorInventory.builder()
                .vendorInventoryId(UUID.randomUUID())
                .batchExecution(batch)
                .vendorId("V001")
                .productCode(productCode)
                .productName("Laptop")
                .quantity(10)
                .unitPrice(BigDecimal.TEN)
                .uploadTime(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldSaveVendorInventory() {

        BatchExecution batch =
                createBatch();

        VendorInventory saved =
                repository.save(
                        createInventory(batch, "P001")
                );

        assertNotNull(saved);
        assertNotNull(
                saved.getVendorInventoryId()
        );
    }

    @Test
    void shouldFindVendorInventoryByBatchId() {

        BatchExecution batch =
                createBatch();

        repository.save(
                createInventory(batch, "P001")
        );

        List<VendorInventory> result =
                repository.findByBatchExecutionBatchId(
                        batch.getBatchId()
                );

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnMultipleRecordsForSameBatch() {

        BatchExecution batch =
                createBatch();

        repository.save(
                createInventory(batch, "P001")
        );

        repository.save(
                createInventory(batch, "P002")
        );

        List<VendorInventory> result =
                repository.findByBatchExecutionBatchId(
                        batch.getBatchId()
                );

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyWhenBatchIdDoesNotExist() {

        List<VendorInventory> result =
                repository.findByBatchExecutionBatchId(
                        UUID.randomUUID()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlyMatchingBatchRecords() {

        BatchExecution batch1 =
                createBatch();

        BatchExecution batch2 =
                createBatch();

        repository.save(
                createInventory(batch1, "P001")
        );

        repository.save(
                createInventory(batch2, "P002")
        );

        List<VendorInventory> result =
                repository.findByBatchExecutionBatchId(
                        batch1.getBatchId()
                );

        assertEquals(1, result.size());

        assertEquals(
                batch1.getBatchId(),
                result.get(0)
                        .getBatchExecution()
                        .getBatchId()
        );
    }

    @Test
    void shouldDeleteVendorInventory() {

        BatchExecution batch =
                createBatch();

        VendorInventory saved =
                repository.save(
                        createInventory(batch, "P001")
                );

        repository.delete(saved);

        assertFalse(
                repository.findById(
                        saved.getVendorInventoryId()
                ).isPresent()
        );
    }

    @Test
    void shouldReturnCorrectCount() {

        BatchExecution batch =
                createBatch();

        repository.save(
                createInventory(batch, "P001")
        );

        repository.save(
                createInventory(batch, "P002")
        );

        assertEquals(
                2,
                repository.count()
        );
    }
}