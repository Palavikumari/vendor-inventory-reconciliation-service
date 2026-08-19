package com.company.virs.util;

import org.springframework.web.multipart.MultipartFile;

public final class FileUtil {

    private FileUtil() {
    }

    public static boolean isCsv(MultipartFile file) {

        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }

        return file.getOriginalFilename()
                .toLowerCase()
                .endsWith(AppConstants.CSV_EXTENSION);
    }

    public static String getFileName(MultipartFile file) {

        if (file == null) {
            return "";
        }

        return file.getOriginalFilename();
    }
}