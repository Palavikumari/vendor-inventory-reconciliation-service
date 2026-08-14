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
@Schema(name = "BatchResponse",description = "Batch execution details")
public class BatchResponse {

    private UUID batchId;

    private String fileName;

    private String executionType;

    private String status;

    private Integer totalRecords;

    private Integer processedRecords;

    private Integer failedRecords;

    private Integer batchSize;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}