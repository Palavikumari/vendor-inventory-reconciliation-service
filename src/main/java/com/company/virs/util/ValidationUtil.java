package com.company.virs.util;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isPositive(Integer value) {
        return value != null && value >= 0;
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}