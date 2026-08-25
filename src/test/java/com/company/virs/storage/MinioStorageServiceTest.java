package com.company.virs.storage;

import com.company.virs.config.secrets.SecretProvider;
import com.company.virs.exception.ValidationException;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private SecretProvider secretProvider;

    @InjectMocks
    private MinioStorageService service;

    @Test
    void uploadFile_ShouldThrow_WhenFileEmpty() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        new byte[0]
                );

        assertThrows(
                ValidationException.class,
                () -> service.uploadFile(
                        file,
                        "test.csv"
                )
        );
    }

    @Test
    void uploadFile_ShouldThrow_WhenObjectNameBlank() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "a.csv",
                        "text/csv",
                        "data".getBytes()
                );

        assertThrows(
                ValidationException.class,
                () -> service.uploadFile(
                        file,
                        ""
                )
        );
    }

    @Test
    void deleteFile_ShouldReturnFalse_WhenNameBlank() {

        assertFalse(
                service.deleteFile("")
        );
    }

    @Test
    void deleteFile_ShouldReturnTrue() {

        when(secretProvider.getMinioBucketName())
                .thenReturn("bucket");

        assertTrue(
                service.deleteFile("test.csv")
        );
    }

    @Test
    void initializeBucket_ShouldThrow_WhenMinioFails()
            throws Exception {

        when(secretProvider.getMinioBucketName())
                .thenReturn("bucket");

        when(
                minioClient.bucketExists(any())
        ).thenThrow(new RuntimeException());

        assertThrows(
                IllegalStateException.class,
                service::initializeBucket
        );
    }

    @Test
    void initializeBucket_ShouldNotCreate_WhenBucketExists()
            throws Exception {

        when(secretProvider.getMinioBucketName())
                .thenReturn("bucket");

        when(minioClient.bucketExists(any()))
                .thenReturn(true);

        assertDoesNotThrow(
                () -> service.initializeBucket()
        );
    }
    @Test
    void deleteFile_ShouldReturnFalse_WhenExceptionOccurs()
            throws Exception {

        when(secretProvider.getMinioBucketName())
                .thenReturn("bucket");

        doThrow(new RuntimeException())
                .when(minioClient)
                .removeObject(any());

        assertFalse(
                service.deleteFile("test.csv")
        );
    }
    @Test
    void uploadFile_ShouldThrowValidationException_WhenUploadFails()
            throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "inventory.csv",
                        "text/csv",
                        "test".getBytes()
                );

        when(secretProvider.getMinioBucketName())
                .thenReturn("bucket");

        doThrow(new RuntimeException())
                .when(minioClient)
                .putObject(any());

        assertThrows(
                ValidationException.class,
                () -> service.uploadFile(
                        file,
                        "inventory.csv"
                )
        );
    }
}