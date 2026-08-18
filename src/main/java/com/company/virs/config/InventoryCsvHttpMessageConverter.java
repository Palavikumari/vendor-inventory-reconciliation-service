package com.company.virs.config;

import com.company.virs.dto.response.InventoryResponse;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class InventoryCsvHttpMessageConverter
        extends AbstractHttpMessageConverter<List<InventoryResponse>> {

    private static final MediaType CSV_MEDIA_TYPE =
            MediaType.parseMediaType("text/csv");

    public InventoryCsvHttpMessageConverter() {
        super(CSV_MEDIA_TYPE);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected List<InventoryResponse> readInternal(
            Class<? extends List<InventoryResponse>> clazz,
            HttpInputMessage inputMessage) {

        throw new UnsupportedOperationException(
                "CSV request parsing is not supported.");
    }

    @Override
    protected void writeInternal(
            List<InventoryResponse> inventoryList,
            HttpOutputMessage outputMessage)
            throws IOException {

        StringBuilder csv = new StringBuilder();

        csv.append(
                "vendorId,sku,productName,vendorQuantity," +
                        "quantityDifference,unitPrice," +
                        "reconciliationStatus,remarks," +
                        "notificationStatus,notificationTime\n");

        for (InventoryResponse inventory : inventoryList) {

            csv.append(escape(inventory.getVendorId()))
                    .append(",")

                    .append(escape(inventory.getSku()))
                    .append(",")

                    .append(escape(inventory.getProductName()))
                    .append(",")

                    .append(value(inventory.getVendorQuantity()))
                    .append(",")

                    .append(value(inventory.getQuantityDifference()))
                    .append(",")

                    .append(value(inventory.getUnitPrice()))
                    .append(",")

                    .append(escape(
                            inventory.getReconciliationStatus()))
                    .append(",")

                    .append(escape(inventory.getRemarks()))
                    .append(",")

                    .append(escape(
                            inventory.getNotificationStatus()))
                    .append(",")

                    .append(value(
                            inventory.getNotificationTime()))
                    .append("\n");
        }

        outputMessage.getHeaders()
                .setContentType(CSV_MEDIA_TYPE);

        outputMessage.getHeaders()
                .setContentDisposition(
                        org.springframework.http.ContentDisposition
                                .attachment()
                                .filename("inventory.csv")
                                .build());

        try (OutputStream outputStream =
                     outputMessage.getBody()) {

            outputStream.write(
                    csv.toString()
                            .getBytes(StandardCharsets.UTF_8));
        }
    }

    private String escape(Object value) {

        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        if (text.contains(",")
                || text.contains("\"")
                || text.contains("\n")) {

            return "\""
                    + text.replace("\"", "\"\"")
                    + "\"";
        }

        return text;
    }

    private String value(Object value) {

        return value == null
                ? ""
                : String.valueOf(value);
    }
}