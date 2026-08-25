package com.company.virs.config;

import com.company.virs.dto.response.ReconciliationResponse;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ReconciliationCsvHttpMessageConverter
        extends AbstractHttpMessageConverter<ReconciliationResponse> {

    private static final MediaType CSV_MEDIA_TYPE =
            new MediaType(
                    "text",
                    "csv",
                    StandardCharsets.UTF_8
            );

    public ReconciliationCsvHttpMessageConverter() {
        super(CSV_MEDIA_TYPE);
    }

    @Override
    protected boolean supports(
            Class<?> clazz) {

        return ReconciliationResponse.class
                .isAssignableFrom(clazz);
    }

    @Override
    protected ReconciliationResponse readInternal(
            Class<? extends ReconciliationResponse> clazz,
            HttpInputMessage inputMessage) {

        throw new UnsupportedOperationException(
                "CSV request body is not supported."
        );
    }

    @Override
    protected void writeInternal(
            ReconciliationResponse response,
            HttpOutputMessage outputMessage)
            throws IOException {

        String csv =
                String.join(
                        "\n",

                        "batchId,fileName,executionType,status,"
                                + "totalRecords,processedRecords,"
                                + "failedRecords,batchSize,"
                                + "discrepancyRecords,startTime,endTime",

                        String.join(
                                ",",
                                value(response.getBatchId()),
                                escape(response.getFileName()),
                                escape(response.getExecutionType()),
                                escape(response.getStatus()),
                                value(response.getTotalRecords()),
                                value(response.getProcessedRecords()),
                                value(response.getFailedRecords()),
                                value(response.getBatchSize()),
                                value(response.getDiscrepancyRecords()),
                                value(response.getStartTime()),
                                value(response.getEndTime())
                        )
                );

        outputMessage
                .getHeaders()
                .setContentType(
                        CSV_MEDIA_TYPE
                );

        outputMessage
                .getBody()
                .write(
                        csv.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    private String value(
            Object value) {

        return value == null
                ? ""
                : value.toString();
    }

    private String escape(
            String value) {

        if (value == null) {
            return "";
        }

        return "\""
                + value.replace(
                "\"",
                "\"\""
        )
                + "\"";
    }
}