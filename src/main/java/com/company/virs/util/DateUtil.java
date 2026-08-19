package com.company.virs.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private DateUtil() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static String format(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.format(
                DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss"));
    }
}