package com.company.virs.service;

import java.util.Optional;

public interface ReferenceInventoryService {

    Optional<Integer> getReferenceQuantity(String sku);
}