package com.company.virs.storage;

import com.company.virs.config.secrets.SecretProvider;
import com.company.virs.exception.ValidationException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final SecretProvider secretProvider;

    @Override
    public String uploadFile(MultipartFile file) {

        try {

            String fileName = file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(secretProvider.getMinioBucketName())
                            .object(fileName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1)
                            .contentType(file.getContentType())
                            .build());

            log.info(
                    "File uploaded successfully to MinIO : {}",
                    fileName);

            return fileName;

        } catch (Exception ex) {

            log.error(
                    "File upload failed",
                    ex);

            throw new ValidationException(
                    "Unable to upload file to MinIO.");
        }
    }

    @Override
    public boolean deleteFile(String fileName) {

        try {

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(secretProvider.getMinioBucketName())
                            .object(fileName)
                            .build());

            log.info(
                    "File deleted successfully : {}",
                    fileName);

            return true;

        } catch (Exception ex) {

            log.error(
                    "Unable to delete file : {}",
                    fileName,
                    ex);

            return false;
        }
    }
}