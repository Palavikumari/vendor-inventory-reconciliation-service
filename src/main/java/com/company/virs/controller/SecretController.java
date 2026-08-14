package com.company.virs.controller;

import com.company.virs.config.secrets.SecretProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/secrets")
public class SecretController {

    private final SecretProvider secretProvider;

    @GetMapping("/test")
    public Map<String, String> testSecrets() {

        Map<String, String> response = new HashMap<>();

        response.put(
                "databaseUsername",
                secretProvider.getDatabaseUsername());

        response.put(
                "databasePassword",
                "********");

        response.put(
                "vendorApiKey",
                secretProvider.getVendorApiKey());

        return response;
    }
}