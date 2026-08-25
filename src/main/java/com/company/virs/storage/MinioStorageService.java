package com.company.virs.storage;

import com.company.virs.config.secrets.SecretProvider;
import com.company.virs.exception.ValidationException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService
        implements StorageService {

    private final MinioClient minioClient;
    private final SecretProvider secretProvider;

    @PostConstruct
    public void initializeBucket() {

        try {

            String bucket =
                    secretProvider.getMinioBucketName();

            boolean exists =
                    minioClient.bucketExists(
                            BucketExistsArgs.builder()
                                    .bucket(bucket)
                                    .build()
                    );

            if (!exists) {

                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );

                log.info(
                        "Created MinIO bucket {}",
                        bucket
                );
            }

        } catch (Exception ex) {

            log.error(
                    "Unable to initialize MinIO bucket",
                    ex
            );

            throw new IllegalStateException(
                    "MinIO bucket initialization failed",
                    ex
            );
        }
    }

    @Override
    public String uploadFile(
            MultipartFile file,
            String objectName) {

        if (file == null || file.isEmpty()) {

            throw new ValidationException(
                    "File cannot be empty."
            );
        }

        if (objectName == null ||
                objectName.isBlank()) {

            throw new ValidationException(
                    "Object name is required."
            );
        }

        try {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(
                                    secretProvider
                                            .getMinioBucketName()
                            )
                            .object(objectName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(
                                    file.getContentType() != null
                                            ? file.getContentType()
                                            : "text/csv"
                            )
                            .build()
            );

            log.info(
                    "Uploaded source file to MinIO: {}",
                    objectName
            );

            return objectName;

        } catch (Exception ex) {

            log.error(
                    "File upload failed for object {}",
                    objectName,
                    ex
            );

            throw new ValidationException(
                    "Unable to upload file to object storage."
            );
        }
    }

    @Override
    public boolean deleteFile(
            String objectName) {

        if (objectName == null ||
                objectName.isBlank()) {

            return false;
        }

        try {

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(
                                    secretProvider
                                            .getMinioBucketName()
                            )
                            .object(objectName)
                            .build()
            );

            log.info(
                    "Deleted MinIO object {}",
                    objectName
            );

            return true;

        } catch (Exception ex) {

            log.error(
                    "Unable to delete file {}",
                    objectName,
                    ex
            );

            return false;
        }
    }
}