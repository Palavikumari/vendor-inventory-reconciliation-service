package com.company.virs.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BatchProcessingResult {

    private int totalRecords;

    private int processedRecords;

    private int failedRecords;
}