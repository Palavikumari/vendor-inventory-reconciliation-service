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

    private final RestTemplate restTemplate =
            new RestTemplate();

    @Override
    public boolean publishNotification(String message) {

        String webhook =
                secretProvider.getSlackWebhookUrl();

        if (webhook == null || webhook.isBlank()) {

            log.warn(
                    "Slack webhook is not configured. "
                            + "Notification was not sent."
            );

            return false;
        }

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<Map<String, String>> request =
                new HttpEntity<>(
                        Map.of("text", message),
                        headers
                );

        try {

            restTemplate.postForEntity(
                    webhook,
                    request,
                    String.class
            );

            log.info("Slack notification published successfully");

            return true;

        } catch (Exception ex) {

            log.error(
                    "Unable to publish Slack notification",
                    ex
            );

            return false;
        }
    }
}