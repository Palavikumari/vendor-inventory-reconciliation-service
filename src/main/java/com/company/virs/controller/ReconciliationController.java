package com.company.virs.controller;

import com.company.virs.dto.response.ReconciliationResponse;
import com.company.virs.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
@Tag(
        name = "Reconciliation API",
        description = "Vendor inventory reconciliation operations"
)
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @Operation(
            summary = "Start vendor inventory reconciliation",
            description = """
                    Accepts a vendor inventory CSV file, validates and parses
                    the records, stores the source file, reconciles vendor
                    quantities against internal reference inventory, persists
                    results and publishes discrepancy notifications.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reconciliation completed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation =
                                            ReconciliationResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid CSV file"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Reconciliation failed"
            )
    })
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReconciliationResponse> reconcile(
            @RequestParam("file")
            MultipartFile file) {

        return ResponseEntity.ok(
                reconciliationService.reconcile(file)
        );
    }

    @Operation(
            summary = "Get reconciliation result",
            description = """
                    Retrieves reconciliation summary for a batch.
                    JSON is returned by default and CSV can be requested
                    using the Accept header.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Result retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Batch not found"
            ),
            @ApiResponse(
                    responseCode = "406",
                    description = "Requested representation is not supported"
            )
    })
    @GetMapping(
            value = "/{batchId}",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    "text/csv"
            }
    )
    public ResponseEntity<ReconciliationResponse> getResult(
            @PathVariable UUID batchId) {

        return ResponseEntity.ok(
                reconciliationService
                        .getReconciliationResult(batchId)
        );
    }

    @Operation(
            summary = "Re-trigger a reconciliation batch",
            description = """
                    Re-processes an existing reconciliation batch.
                    The source batchId identifies the batch to re-trigger.
                    Each retrigger creates a new RETRIGGER execution.
                    No Idempotency-Key is required.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Batch re-triggered successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Batch not found"
            )
    })
    @PostMapping(
            value = "/{batchId}/retrigger",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ReconciliationResponse> retrigger(
            @PathVariable UUID batchId) {

        return ResponseEntity.ok(
                reconciliationService.retrigger(batchId)
        );
    }
}