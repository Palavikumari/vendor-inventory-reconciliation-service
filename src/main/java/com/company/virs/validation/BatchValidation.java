package com.company.virs.validation;

import com.company.virs.dto.request.BatchRequest;
import com.company.virs.exception.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BatchValidation {

    public void validate(BatchRequest request) {

        if (request == null) {
            throw new ValidationException("Batch request cannot be null.");
        }

        if (!StringUtils.hasText(request.getFileName())) {
            throw new ValidationException("File name is required.");
        }

        if (!StringUtils.hasText(request.getExecutionType())) {
            throw new ValidationException("Execution type is required.");
        }

        if (!request.getFileName().toLowerCase().endsWith(".csv")) {
            throw new ValidationException("Only CSV files are supported.");
        }
    }
}