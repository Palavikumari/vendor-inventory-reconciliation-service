package com.company.virs.validation;

import com.company.virs.exception.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvValidation {

    public void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ValidationException("CSV file cannot be empty.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new ValidationException(
                    "Invalid file type. Only CSV files are allowed.");
        }

        if (file.getSize() == 0) {
            throw new ValidationException(
                    "Uploaded CSV file is empty.");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ValidationException(
                    "CSV file size cannot exceed 10 MB.");
        }
    }
}