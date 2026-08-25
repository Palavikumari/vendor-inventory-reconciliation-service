package com.company.virs.parser;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class CsvParser {

    private static final List<String> REQUIRED_HEADERS =
            List.of(
                    "vendorId",
                    "productCode",
                    "productName",
                    "quantity",
                    "unitPrice"
            );

    public List<InventoryRequest> parse(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ValidationException(
                    "CSV file cannot be empty."
            );
        }

        List<InventoryRequest> records =
                new ArrayList<>();

        Set<String> seenProductCodes =
                new HashSet<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        );

                CSVParser parser =
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreEmptyLines(true)
                                .setTrim(true)
                                .build()
                                .parse(reader)
        ) {

            validateHeaders(parser);

            for (CSVRecord record : parser) {

                String vendorId =
                        getRequired(
                                record,
                                "vendorId"
                        );

                String productCode =
                        getRequired(
                                record,
                                "productCode"
                        );

                String productName =
                        getRequired(
                                record,
                                "productName"
                        );

                String quantityValue =
                        getRequired(
                                record,
                                "quantity"
                        );

                String unitPriceValue =
                        getRequired(
                                record,
                                "unitPrice"
                        );

                if (!seenProductCodes.add(productCode)) {

                    throw new ValidationException(
                            "Duplicate product code found at CSV record "
                                    + record.getRecordNumber()
                                    + ": "
                                    + productCode
                    );
                }

                Integer quantity;

                BigDecimal unitPrice;

                try {

                    quantity =
                            Integer.valueOf(
                                    quantityValue
                            );

                    unitPrice =
                            new BigDecimal(
                                    unitPriceValue
                            );

                } catch (NumberFormatException ex) {

                    throw new ValidationException(
                            "Invalid numeric value at CSV record "
                                    + record.getRecordNumber()
                    );
                }

                if (quantity < 0) {

                    throw new ValidationException(
                            "Quantity cannot be negative at CSV record "
                                    + record.getRecordNumber()
                    );
                }

                if (unitPrice.signum() < 0) {

                    throw new ValidationException(
                            "Unit price cannot be negative at CSV record "
                                    + record.getRecordNumber()
                    );
                }

                records.add(
                        InventoryRequest.builder()
                                .vendorId(vendorId)
                                .productCode(productCode)
                                .productName(productName)
                                .quantity(quantity)
                                .unitPrice(unitPrice)
                                .build()
                );
            }

        } catch (ValidationException ex) {

            throw ex;

        } catch (IOException ex) {

            log.error(
                    "Unable to read CSV file",
                    ex
            );

            throw new ValidationException(
                    "Unable to read CSV file."
            );

        } catch (Exception ex) {

            log.error(
                    "Invalid CSV format",
                    ex
            );

            throw new ValidationException(
                    "Invalid CSV file format."
            );
        }

        if (records.isEmpty()) {

            throw new ValidationException(
                    "CSV file does not contain any inventory records."
            );
        }

        return records;
    }

    private void validateHeaders(
            CSVParser parser) {

        List<String> headers =
                parser.getHeaderNames()
                        .stream()
                        .map(String::trim)
                        .toList();

        List<String> missing =
                REQUIRED_HEADERS.stream()
                        .filter(header ->
                                !headers.contains(header))
                        .toList();

        if (!missing.isEmpty()) {

            throw new ValidationException(
                    "Invalid CSV headers. Missing required columns: "
                            + missing
            );
        }
    }

    private String getRequired(
            CSVRecord record,
            String column) {

        String value;

        try {

            value = record.get(column);

        } catch (IllegalArgumentException ex) {

            throw new ValidationException(
                    "Required CSV column is missing: "
                            + column
            );
        }

        if (value == null || value.isBlank()) {

            throw new ValidationException(
                    "CSV contains missing required value '"
                            + column
                            + "' at record "
                            + record.getRecordNumber()
            );
        }

        return value.trim();
    }
}