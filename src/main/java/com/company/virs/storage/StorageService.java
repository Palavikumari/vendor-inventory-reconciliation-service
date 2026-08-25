package com.company.virs.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadFile(
            MultipartFile file,
            String objectName
    );

    boolean deleteFile(
            String objectName
    );
}