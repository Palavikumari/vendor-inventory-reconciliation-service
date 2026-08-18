package com.company.virs.parser;

import com.company.virs.dto.request.InventoryRequest;
import com.company.virs.exception.ValidationException;
import com.company.virs.util.CsvConstants;
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
import java.util.List;

@Component
@Slf4j
public class CsvParserImpl implements CsvParser {

    @Override
    public List<InventoryRequest> parse(
            MultipartFile file) {

        List<InventoryRequest> inventoryList =
                new ArrayList<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8));

                CSVParser csvParser =
                        new CSVParser(
                                reader,
                                CSVFormat.DEFAULT
                                        .builder()
                                        .setHeader()
                                        .setSkipHeaderRecord(true)
                                        .setIgnoreEmptyLines(true)
                                        .setTrim(true)
                                        .build())
        ) {

            validateHeaders(csvParser);

            for (CSVRecord record : csvParser) {

                if (record.size() == 0) {
                    continue;
                }

                String vendorId =
                        record.get(CsvConstants.VENDOR_ID);

                String sku =
                        record.get(CsvConstants.SKU);

                String productName =
                        record.get(
                                CsvConstants.PRODUCT_NAME);

                String quantityValue =
                        record.get(CsvConstants.QUANTITY);

                String unitPriceValue =
                        record.get(CsvConstants.UNIT_PRICE);

                if (vendorId == null
                        || vendorId.isBlank()
                        || sku == null
                        || sku.isBlank()
                        || productName == null
                        || productName.isBlank()
                        || quantityValue == null
                        || quantityValue.isBlank()
                        || unitPriceValue == null
                        || unitPriceValue.isBlank()) {

                    throw new ValidationException(
                            "CSV contains missing required values at record "
                                    + record.getRecordNumber());
                }

                Integer quantity;

                BigDecimal unitPrice;

                try {

                    quantity =
                            Integer.parseInt(
                                    quantityValue);

                    unitPrice =
                            new BigDecimal(
                                    unitPriceValue);

                } catch (NumberFormatException ex) {

                    throw new ValidationException(
                            "Invalid numeric value at CSV record "
                                    + record.getRecordNumber());
                }

                if (quantity < 0) {

                    throw new ValidationException(
                            "Quantity cannot be negative at CSV record "
                                    + record.getRecordNumber());
                }

                if (unitPrice.compareTo(
                        BigDecimal.ZERO) < 0) {

                    throw new ValidationException(
                            "Unit price cannot be negative at CSV record "
                                    + record.getRecordNumber());
                }

                InventoryRequest request =
                        InventoryRequest.builder()
                                .vendorId(vendorId)
                                .sku(sku)
                                .productName(productName)
                                .quantity(quantity)
                                .unitPrice(unitPrice)
                                .build();

                inventoryList.add(request);
            }

            log.info(
                    "Successfully parsed {} inventory records.",
                    inventoryList.size());

            return inventoryList;

        } catch (ValidationException ex) {

            throw ex;

        } catch (IOException ex) {

            log.error(
                    "Error while reading CSV file",
                    ex);

            throw new ValidationException(
                    "Unable to read CSV file.");

        } catch (Exception ex) {

            log.error(
                    "Invalid CSV format",
                    ex);

            throw new ValidationException(
                    "Invalid CSV file format.");
        }
    }

    private void validateHeaders(
            CSVParser csvParser) {

        List<String> headers =
                csvParser.getHeaderNames();

        List<String> requiredHeaders =
                List.of(
                        CsvConstants.VENDOR_ID,
                        CsvConstants.SKU,
                        CsvConstants.PRODUCT_NAME,
                        CsvConstants.QUANTITY,
                        CsvConstants.UNIT_PRICE);

        List<String> missingHeaders =
                requiredHeaders.stream()
                        .filter(header ->
                                !headers.contains(header))
                        .toList();

        if (!missingHeaders.isEmpty()) {

            throw new ValidationException(
                    "Invalid CSV headers. Missing required columns: "
                            + missingHeaders
                            + ". Expected columns: "
                            + requiredHeaders);
        }
    }
}