package com.company.virs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "BatchRequest",
        description = "Request payload used to create a new inventory processing batch"
        )
public class BatchRequest {

    @NotBlank(message = "File name is required")
    @Schema(
            description = "CSV file name",
            example = "inventory.csv"
    )
    private String fileName;

    @NotBlank(message = "Execution type is required")
    @Schema(
            description = "Execution type",
            example = "FULL"
    )
    private String executionType;
}