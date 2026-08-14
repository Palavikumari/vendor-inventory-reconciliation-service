package com.company.virs.config;

import com.company.virs.config.secrets.SecretProvider;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final SecretProvider secretProvider;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(secretProvider.getMinioEndpoint())
                .credentials(
                        secretProvider.getMinioAccessKey(),
                        secretProvider.getMinioSecretKey())
                .build();
    }
}