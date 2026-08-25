package com.company.virs.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ReconciliationResponse",
        description = "Summary of a reconciliation batch"
)
public class ReconciliationResponse {

    @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID batchId;

    @Schema(example = "vendor_inventory.csv")
    private String fileName;

    @Schema(example = "INITIAL")
    private String executionType;

    @Schema(example = "COMPLETED")
    private String status;

    @Schema(example = "1500")
    private Integer totalRecords;

    @Schema(example = "1500")
    private Integer processedRecords;

    @Schema(example = "0")
    private Integer failedRecords;

    @Schema(example = "500")
    private Integer batchSize;

    @Schema(example = "50")
    private Integer discrepancyRecords;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String notificationStatus;
}