package com.company.virs.controller;

import com.company.virs.dto.request.BatchRequest;
import com.company.virs.dto.response.BatchResponse;
import com.company.virs.dto.response.UploadResponse;
import com.company.virs.service.BatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Batch API",
        description = "Batch Management APIs"
)
@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @Operation(
            summary = "Create Inventory Processing Batch",
            description =
                    """
                    Creates a new batch execution record.
    
                    A batch tracks lifecycle of vendor inventory
                    processing from upload through reconciliation.
    
                    Execution Types:
    
                    FULL
                    INCREMENTAL
                    MANUAL
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Batch created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate batch"
            )
    })
    @PostMapping
    public ResponseEntity<UploadResponse> createBatch(
            @Valid @RequestBody BatchRequest request) {

        UploadResponse response =
                batchService.createBatch(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Get batch details")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Batch found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Batch not found"
            )
    })
    @GetMapping("/{batchId}")
    public ResponseEntity<BatchResponse> getBatchById(
            @PathVariable UUID batchId) {

        return ResponseEntity.ok(
                batchService.getBatchById(batchId));
    }

    @Operation(summary = "Get batch status")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Batch status retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Batch not found"
            )
    })
    @GetMapping("/{batchId}/status")
    public ResponseEntity<BatchResponse> getBatchStatus(
            @PathVariable UUID batchId) {

        return ResponseEntity.ok(
                batchService.getBatchStatus(batchId));
    }
    @PostMapping("/{batchId}/retry")
    public ResponseEntity<BatchResponse> retryBatch(
            @PathVariable UUID batchId) {

        return ResponseEntity.ok(
                batchService.retryBatch(batchId));
    }
}