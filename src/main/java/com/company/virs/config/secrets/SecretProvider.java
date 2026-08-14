package com.company.virs.config.secrets;

public interface SecretProvider {

    String getDatabaseUsername();

    String getDatabasePassword();

    String getVendorApiKey();

    String getMinioEndpoint();

    String getMinioAccessKey();

    String getMinioSecretKey();

    String getMinioBucketName();

    String getSlackWebhookUrl();
}