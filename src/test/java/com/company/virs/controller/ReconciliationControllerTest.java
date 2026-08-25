package com.company.virs.controller;

import com.company.virs.dto.response.ReconciliationResponse;
import com.company.virs.service.ReconciliationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReconciliationController.class)
class ReconciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReconciliationService reconciliationService;

    @Test
    void reconcile_ShouldReturn200() throws Exception {

        ReconciliationResponse response =
                new ReconciliationResponse();

        when(reconciliationService.reconcile(any()))
                .thenReturn(response);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "product,qty".getBytes()
                );

        mockMvc.perform(
                        multipart("/api/v1/reconciliation")
                                .file(file)
                )
                .andExpect(status().isOk());

        verify(reconciliationService, times(1))
                .reconcile(any());
    }

    @Test
    void getResult_ShouldReturn200() throws Exception {

        UUID batchId = UUID.randomUUID();

        ReconciliationResponse response =
                new ReconciliationResponse();

        when(reconciliationService
                .getReconciliationResult(batchId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/reconciliation/{batchId}",
                                batchId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        verify(reconciliationService, times(1))
                .getReconciliationResult(batchId);
    }

    @Test
    void retrigger_ShouldReturn200() throws Exception {

        UUID batchId = UUID.randomUUID();

        ReconciliationResponse response =
                new ReconciliationResponse();

        when(reconciliationService.retrigger(batchId))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/reconciliation/{batchId}/retrigger",
                                batchId)
                )
                .andExpect(status().isOk());

        verify(reconciliationService, times(1))
                .retrigger(batchId);
    }

    @Test
    void getResult_ShouldPassCorrectBatchId() throws Exception {

        UUID batchId = UUID.randomUUID();

        when(reconciliationService
                .getReconciliationResult(batchId))
                .thenReturn(new ReconciliationResponse());

        mockMvc.perform(
                        get("/api/v1/reconciliation/{batchId}",
                                batchId)
                )
                .andExpect(status().isOk());

        verify(reconciliationService)
                .getReconciliationResult(eq(batchId));
    }

    @Test
    void retrigger_ShouldPassCorrectBatchId() throws Exception {

        UUID batchId = UUID.randomUUID();

        when(reconciliationService.retrigger(batchId))
                .thenReturn(new ReconciliationResponse());

        mockMvc.perform(
                        post("/api/v1/reconciliation/{batchId}/retrigger",
                                batchId)
                )
                .andExpect(status().isOk());

        verify(reconciliationService)
                .retrigger(eq(batchId));
    }
}