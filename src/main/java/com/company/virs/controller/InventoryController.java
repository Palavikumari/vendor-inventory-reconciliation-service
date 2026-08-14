package com.company.virs.controller;

import com.company.virs.dto.response.InventoryResponse;
import com.company.virs.dto.response.UploadResponse;
import com.company.virs.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Inventory API",
        description = "Vendor Inventory APIs"
)
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(
            summary =
                    "Upload Vendor Inventory CSV",
            description =
                    """
                    Uploads vendor inventory records.
    
                    File is stored in MinIO.
    
                    Records are validated,
                    parsed and persisted
                    into VendorInventory.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Inventory uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid CSV file"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Batch not found"
            )
    })
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UploadResponse> uploadInventory(
            @RequestParam("file") MultipartFile file,
            @RequestParam UUID batchId) {

        UploadResponse response =
                inventoryService.uploadInventory(
                        batchId,
                        file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get inventory by batch",
            description =
                    "Returns inventory records in JSON or CSV format based on Accept header"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Batch not found"
            )
    })
    @GetMapping(
            value = "/batch/{batchId}",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    "text/csv"
            }
    )
    public ResponseEntity<?> getInventoryByBatch(
            @PathVariable UUID batchId,
            @RequestHeader(
                    value = HttpHeaders.ACCEPT,
                    required = false)
            String acceptHeader) {

        List<InventoryResponse> inventoryList =
                inventoryService.getInventoryByBatch(
                        batchId);

        if (acceptHeader != null
                && acceptHeader.contains("text/csv")) {

            StringBuilder csv =
                    new StringBuilder();

            csv.append(
                    "vendorId,sku,productName,vendorQuantity,reconciliationStatus,quantityDifference,remarks\n");

            for (InventoryResponse inventory : inventoryList) {

                csv.append(
                                inventory.getVendorId())
                        .append(",")

                        .append(
                                inventory.getSku())
                        .append(",")

                        .append(
                                inventory.getProductName())
                        .append(",")

                        .append(
                                inventory.getVendorQuantity())
                        .append(",")

                        .append(
                                inventory.getReconciliationStatus())
                        .append(",")

                        .append(
                                inventory.getQuantityDifference())
                        .append(",")

                        .append(
                                inventory.getRemarks())
                        .append("\n");
            }

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=inventory.csv")
                    .contentType(
                            MediaType.parseMediaType(
                                    "text/csv"))
                    .body(csv.toString());
        }

        return ResponseEntity.ok(
                inventoryList);
    }

    @Operation(
            summary = "Get inventory by SKU",
            description = "Returns inventory details for a specific SKU"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found"
            )
    })
    @GetMapping("/batch/{batchId}/sku/{sku}")
    public ResponseEntity<InventoryResponse> getInventoryBySku(
            @PathVariable UUID batchId,
            @PathVariable String sku) {

        return ResponseEntity.ok(
                inventoryService.getInventoryBySku(
                        batchId,
                        sku));
    }
}