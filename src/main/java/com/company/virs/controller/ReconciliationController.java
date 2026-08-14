package com.company.virs.controller;

import com.company.virs.dto.response.InventoryResponse;
import com.company.virs.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Reconciliation API",
        description = "Inventory Reconciliation APIs"
)
@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @Operation(
            summary =
                    "Run Inventory Reconciliation",
            description =
                    """
                    Compares Vendor Inventory
                    against Internal Inventory.
    
                    Generates:
    
                    MATCHED
    
                    MISMATCH
    
                    MISSING
    
                    statuses.
    
                    Slack notifications are sent
                    for discrepancies.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reconciliation completed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Batch not found"
            )
    })
    @PostMapping("/{batchId}")
    public ResponseEntity<List<InventoryResponse>> reconcileBatch(
            @PathVariable UUID batchId) {

        return ResponseEntity.ok(
                reconciliationService.reconcileBatch(batchId));
    }
}