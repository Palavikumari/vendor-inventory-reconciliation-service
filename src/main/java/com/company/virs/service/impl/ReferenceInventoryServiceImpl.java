package com.company.virs.service.impl;

import com.company.virs.service.ReferenceInventoryService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ReferenceInventoryServiceImpl
        implements ReferenceInventoryService {

    /*
     * Temporary local reference inventory.
     *
     * This represents an external/master-data source.
     */
    private final Map<String, Integer> inventoryReference =
            Map.of(
                    "SKU100", 100,
                    "SKU200", 100,
                    "SKU400", 50,
                    "SKU500", 200
            );

    @Override
    public Optional<Integer> getReferenceQuantity(
            String sku) {

        if (sku == null || sku.isBlank()) {

            return Optional.empty();
        }

        return Optional.ofNullable(
                inventoryReference.get(sku));
    }
}