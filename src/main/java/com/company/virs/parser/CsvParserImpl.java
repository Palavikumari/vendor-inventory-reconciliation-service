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
    public List<InventoryRequest> parse(MultipartFile file) {

        List<InventoryRequest> inventoryList = new ArrayList<>();

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
                                        .build())
        ) {

            for (CSVRecord record : csvParser) {

                InventoryRequest request =
                        InventoryRequest.builder()
                                .vendorId(record.get(CsvConstants.VENDOR_ID))
                                .sku(record.get("sku"))
                                .productName(record.get("productName"))
                                .quantity(
                                        Integer.parseInt(
                                                record.get("quantity")))
                                .unitPrice(
                                        new BigDecimal(
                                                record.get("unitPrice")))
                                .build();

                inventoryList.add(request);
            }

            log.info(
                    "Successfully parsed {} records.",
                    inventoryList.size());

            return inventoryList;

        } catch (IOException ex) {

            log.error(
                    "Error while reading CSV file",
                    ex);

            throw new ValidationException(
                    "Unable to parse CSV file.");

        } catch (Exception ex) {

            log.error(
                    "Invalid CSV format",
                    ex);

            throw new ValidationException(
                    "Invalid CSV file format.");
        }
    }
}