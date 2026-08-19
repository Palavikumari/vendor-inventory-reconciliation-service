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
@Schema(name = "UploadResponse", description = "Response after vendor file upload")
public class UploadResponse {

    private UUID batchId;

    private String fileName;

    private String status;

    private String message;

    private LocalDateTime uploadedAt;
}