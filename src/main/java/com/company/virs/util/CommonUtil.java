package com.company.virs.util;

import java.util.UUID;

public final class CommonUtil {

    private CommonUtil() {
    }

    public static UUID generateUUID() {
        return UUID.randomUUID();
    }
}