package com.company.virs.service;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.dto.response.ReconciliationResponse;
import com.company.virs.entity.BatchExecution;
import com.company.virs.entity.VendorInventory;
import com.company.virs.exception.ResourceNotFoundException;
import com.company.virs.exception.ValidationException;
import com.company.virs.parser.CsvParser;
import com.company.virs.repository.BatchExecutionRepository;
import com.company.virs.repository.InternalInventoryRepository;
import com.company.virs.repository.VendorInventoryRepository;
import com.company.virs.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private BatchExecutionRepository batchRepository;

    @Mock
    private VendorInventoryRepository vendorRepository;

    @Mock
    private InternalInventoryRepository internalInventoryRepository;

    @Mock
    private CsvParser csvParser;

    @Mock
    private StorageService storageService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private ReconciliationService service;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                service,
                "batchSize",
                10
        );

        ReflectionTestUtils.setField(
                service,
                "maxFileSize",
                10485760L
        );
    }

    @Test
    void reconcile_ShouldThrowException_WhenFileIsNull() {

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> service.reconcile(null)
                );

        assertEquals(
                "CSV file cannot be empty.",
                exception.getMessage()
        );
    }

    @Test
    void reconcile_ShouldThrowException_WhenFileIsEmpty() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        new byte[0]
                );

        assertThrows(
                ValidationException.class,
                () -> service.reconcile(file)
        );
    }

    @Test
    void reconcile_ShouldThrowException_WhenFileIsNotCsv() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.txt",
                        "text/plain",
                        "data".getBytes()
                );

        ValidationException exception =
                assertThrows(
                        ValidationException.class,
                        () -> service.reconcile(file)
                );

        assertEquals(
                "Only CSV files are supported.",
                exception.getMessage()
        );
    }

    @Test
    void reconcile_ShouldThrowException_WhenFileTooLarge() {

        ReflectionTestUtils.setField(
                service,
                "maxFileSize",
                1L
        );

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "large-data".getBytes()
                );

        assertThrows(
                ValidationException.class,
                () -> service.reconcile(file)
        );
    }

    @Test
    void reconcile_ShouldUploadAndProcessSuccessfully()
            throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "content".getBytes()
                );

        BatchExecution batch =
                BatchExecution.builder()
                        .batchId(UUID.randomUUID())
                        .fileName("inventory.csv")
                        .build();

        InventoryRequest request =
                InventoryRequest.builder()
                        .vendorId("V001")
                        .productCode("P001")
                        .productName("Laptop")
                        .quantity(10)
                        .unitPrice(BigDecimal.TEN)
                        .build();

        Future<List<?>> future = mock(Future.class);

        when(batchRepository.save(any()))
                .thenReturn(batch);

        when(csvParser.parse(file))
                .thenReturn(List.of(request));

        when(internalInventoryRepository
                .findQuantitiesByProductCode(anyList()))
                .thenReturn(Map.of("P001", 10));

        when(executorService.submit(any(Callable.class)))
                .thenReturn((Future) future);

        when(future.get())
                .thenReturn(List.of());

        ReconciliationResponse response =
                service.reconcile(file);

        assertNotNull(response);

        verify(storageService)
                .uploadFile(any(), anyString());

        verify(vendorRepository, atLeastOnce())
                .saveAll(anyList());

        verify(notificationService)
                .notifyDiscrepancies(anyList());
    }

    @Test
    void reconcile_ShouldMarkBatchFailed_WhenUploadFails() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "content".getBytes()
                );

        BatchExecution batch =
                BatchExecution.builder()
                        .batchId(UUID.randomUUID())
                        .fileName("inventory.csv")
                        .build();

        when(batchRepository.save(any()))
                .thenReturn(batch);

        doThrow(new RuntimeException("Upload error"))
                .when(storageService)
                .uploadFile(any(), anyString());

        assertThrows(
                RuntimeException.class,
                () -> service.reconcile(file)
        );

        verify(batchRepository, atLeast(2))
                .save(any());
    }

    @Test
    void getReconciliationResult_ShouldReturnResult() {

        UUID batchId = UUID.randomUUID();

        BatchExecution batch =
                BatchExecution.builder()
                        .batchId(batchId)
                        .fileName("test.csv")
                        .status("COMPLETED")
                        .build();

        when(batchRepository.findById(batchId))
                .thenReturn(Optional.of(batch));

        when(vendorRepository
                .findByBatchExecutionBatchId(batchId))
                .thenReturn(List.of());

        ReconciliationResponse response =
                service.getReconciliationResult(batchId);

        assertNotNull(response);
        assertEquals(batchId, response.getBatchId());
    }

    @Test
    void getReconciliationResult_ShouldThrowException_WhenBatchMissing() {

        UUID batchId = UUID.randomUUID();

        when(batchRepository.findById(batchId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getReconciliationResult(batchId)
        );
    }

    @Test
    void retrigger_ShouldThrowException_WhenSourceBatchNotFound() {

        UUID batchId = UUID.randomUUID();

        when(batchRepository.findById(batchId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.retrigger(batchId)
        );
    }

    @Test
    void retrigger_ShouldThrowException_WhenSourceRecordsMissing() {

        UUID batchId = UUID.randomUUID();

        BatchExecution batch =
                BatchExecution.builder()
                        .batchId(batchId)
                        .fileName("inventory.csv")
                        .build();

        when(batchRepository.findById(batchId))
                .thenReturn(Optional.of(batch));

        when(vendorRepository
                .findByBatchExecutionBatchId(batchId))
                .thenReturn(List.of());

        assertThrows(
                ValidationException.class,
                () -> service.retrigger(batchId)
        );
    }

    @Test
    void retrigger_ShouldProcessSuccessfully()
            throws Exception {

        UUID sourceBatchId = UUID.randomUUID();

        BatchExecution sourceBatch =
                BatchExecution.builder()
                        .batchId(sourceBatchId)
                        .fileName("inventory.csv")
                        .build();

        VendorInventory inventory =
                VendorInventory.builder()
                        .vendorInventoryId(UUID.randomUUID())
                        .vendorId("V001")
                        .productCode("P001")
                        .productName("Laptop")
                        .quantity(10)
                        .unitPrice(BigDecimal.TEN)
                        .build();

        Future<List<?>> future = mock(Future.class);

        when(batchRepository.findById(sourceBatchId))
                .thenReturn(Optional.of(sourceBatch));

        when(vendorRepository
                .findByBatchExecutionBatchId(sourceBatchId))
                .thenReturn(List.of(inventory));

        when(batchRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(internalInventoryRepository
                .findQuantitiesByProductCode(anyList()))
                .thenReturn(Map.of("P001", 10));

        when(executorService.submit(any(Callable.class)))
                .thenReturn((Future) future);

        when(future.get())
                .thenReturn(List.of());

        ReconciliationResponse response =
                service.retrigger(sourceBatchId);

        assertNotNull(response);

        verify(vendorRepository, atLeastOnce())
                .saveAll(anyList());

        verify(notificationService)
                .notifyDiscrepancies(anyList());
    }

    @Test
    void getReconciliationResult_ShouldReturnSentNotificationStatus_WhenNoDiscrepancies() {

        UUID batchId = UUID.randomUUID();

        BatchExecution batch =
                BatchExecution.builder()
                        .batchId(batchId)
                        .fileName("test.csv")
                        .status("COMPLETED")
                        .build();

        VendorInventory inventory =
                VendorInventory.builder()
                        .vendorInventoryId(UUID.randomUUID())
                        .reconciliationStatus("MATCHED")
                        .build();

        when(batchRepository.findById(batchId))
                .thenReturn(Optional.of(batch));

        when(vendorRepository.findByBatchExecutionBatchId(batchId))
                .thenReturn(List.of(inventory));

        ReconciliationResponse response =
                service.getReconciliationResult(batchId);

        assertEquals(
                "SENT",
                response.getNotificationStatus()
        );
    }

    @Test
    void getReconciliationResult_ShouldReturnFailedNotificationStatus() {

        UUID batchId = UUID.randomUUID();

        BatchExecution batch =
                BatchExecution.builder()
                        .batchId(batchId)
                        .fileName("test.csv")
                        .status("COMPLETED")
                        .build();

        VendorInventory inventory =
                VendorInventory.builder()
                        .vendorInventoryId(UUID.randomUUID())
                        .reconciliationStatus("MISMATCH")
                        .notificationStatus("FAILED")
                        .build();

        when(batchRepository.findById(batchId))
                .thenReturn(Optional.of(batch));

        when(vendorRepository.findByBatchExecutionBatchId(batchId))
                .thenReturn(List.of(inventory));

        ReconciliationResponse response =
                service.getReconciliationResult(batchId);

        assertEquals(
                "FAILED",
                response.getNotificationStatus()
        );
    }
}