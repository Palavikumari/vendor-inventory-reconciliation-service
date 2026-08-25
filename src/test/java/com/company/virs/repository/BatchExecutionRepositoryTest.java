package com.company.virs.repository;

import com.company.virs.entity.BatchExecution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.flyway.enabled=false"
        }
)
class BatchExecutionRepositoryTest {

    @Autowired
    private BatchExecutionRepository repository;

    private BatchExecution createBatch() {

        return BatchExecution.builder()
                .batchId(UUID.randomUUID())
                .fileName("inventory.csv")
                .executionType("INITIAL")
                .status("RUNNING")
                .batchSize(100)
                .totalRecords(0)
                .processedRecords(0)
                .failedRecords(0)
                .startTime(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldSaveBatchExecution() {

        BatchExecution saved =
                repository.save(createBatch());

        assertNotNull(saved);
        assertNotNull(saved.getBatchId());
    }

    @Test
    void shouldFindBatchExecutionById() {

        BatchExecution saved =
                repository.save(createBatch());

        BatchExecution found =
                repository.findById(saved.getBatchId())
                        .orElse(null);

        assertNotNull(found);
        assertEquals(
                saved.getBatchId(),
                found.getBatchId()
        );
    }

    @Test
    void shouldReturnEmptyWhenBatchNotFound() {

        assertTrue(
                repository.findById(
                        UUID.randomUUID()
                ).isEmpty()
        );
    }

    @Test
    void shouldReturnAllBatchExecutions() {

        repository.save(createBatch());
        repository.save(createBatch());

        assertEquals(
                2,
                repository.findAll().size()
        );
    }

    @Test
    void shouldDeleteBatchExecution() {

        BatchExecution saved =
                repository.save(createBatch());

        UUID batchId =
                saved.getBatchId();

        repository.deleteById(batchId);

        assertFalse(
                repository.existsById(batchId)
        );
    }

    @Test
    void shouldCheckExistsById() {

        BatchExecution saved =
                repository.save(createBatch());

        assertTrue(
                repository.existsById(
                        saved.getBatchId()
                )
        );
    }

    @Test
    void shouldReturnFalseForNonExistingId() {

        assertFalse(
                repository.existsById(
                        UUID.randomUUID()
                )
        );
    }

    @Test
    void shouldReturnCorrectCount() {

        repository.save(createBatch());
        repository.save(createBatch());

        assertEquals(
                2,
                repository.count()
        );
    }

    @Test
    void shouldDeleteAllRecords() {

        repository.save(createBatch());
        repository.save(createBatch());

        repository.deleteAll();

        assertEquals(
                0,
                repository.count()
        );
    }
}