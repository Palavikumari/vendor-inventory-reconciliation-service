package com.company.virs.config.secrets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalSecretProvider implements SecretProvider {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${virs.vendor.api-key}")
    private String apiKey;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${slack.webhook-url:}")
    private String slackWebhookUrl;

    @Override
    public String getDatabaseUsername() {
        return username;
    }

    @Override
    public String getDatabasePassword() {
        return password;
    }

    @Override
    public String getVendorApiKey() {
        return apiKey;
    }

    @Override
    public String getMinioEndpoint() {
        return minioEndpoint;
    }

    @Override
    public String getMinioAccessKey() {
        return minioAccessKey;
    }

    @Override
    public String getMinioSecretKey() {
        return minioSecretKey;
    }

    @Override
    public String getMinioBucketName() {
        return bucketName;
    }

    @Override
    public String getSlackWebhookUrl() {
        return slackWebhookUrl;
    }
}