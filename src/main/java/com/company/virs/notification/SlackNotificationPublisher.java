package com.company.virs.notification;

import com.company.virs.config.secrets.SecretProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationPublisher
        implements NotificationPublisher {

    private final SecretProvider secretProvider;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void publishNotification(String message) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload =
                Map.of("text", message);

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(payload, headers);

        try {

            restTemplate.postForEntity(
                    secretProvider.getSlackWebhookUrl(),
                    request,
                    String.class);

            log.info(
                    "Slack notification sent successfully");

        } catch (Exception ex) {

            log.error(
                    "Failed to publish Slack notification",
                    ex);

            throw ex;
        }
    }
}