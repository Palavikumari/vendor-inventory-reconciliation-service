package com.company.virs.controller;

import com.company.virs.dto.response.InventoryResponse;
import com.company.virs.dto.response.UploadResponse;
import com.company.virs.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Inventory API",
        description = "Vendor inventory management and query APIs"
)
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(
            summary = "Upload Vendor Inventory CSV",
            description = """
                    Uploads a vendor inventory CSV file.

                    The file is validated, parsed and stored
                    in the configured object storage.

                    Inventory records are then persisted against
                    the specified processing batch.
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
            @RequestParam("file")
            MultipartFile file,

            @RequestParam
            UUID batchId) {

        UploadResponse response =
                inventoryService.uploadInventory(
                        batchId,
                        file);

        return ResponseEntity
                .status(201)
                .body(response);
    }

    @Operation(
            summary = "Get inventory by batch",
            description = """
                    Returns all inventory records associated
                    with the specified batch.

                    The response representation is selected
                    using HTTP content negotiation.

                    Supported representations:

                    application/json
                    text/csv

                    JSON is returned by default.

                    Examples:

                    Accept: application/json

                    Accept: text/csv

                    Alternatively:

                    ?format=json

                    ?format=csv
                    """
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
    public ResponseEntity<List<InventoryResponse>>
    getInventoryByBatch(
            @Parameter(
                    description = "Processing batch identifier",
                    required = true
            )
            @PathVariable UUID batchId) {

        List<InventoryResponse> response =
                inventoryService.getInventoryByBatch(
                        batchId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get inventory by SKU",
            description =
                    "Returns inventory details for a specific SKU within a batch."
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
    @GetMapping(
            "/batch/{batchId}/sku/{sku}"
    )
    public ResponseEntity<InventoryResponse>
    getInventoryBySku(
            @PathVariable UUID batchId,
            @PathVariable String sku) {

        return ResponseEntity.ok(
                inventoryService.getInventoryBySku(
                        batchId,
                        sku));
    }
}