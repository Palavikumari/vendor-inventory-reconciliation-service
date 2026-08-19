package com.company.virs.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadFile(MultipartFile file);

    boolean deleteFile(String fileName);
}