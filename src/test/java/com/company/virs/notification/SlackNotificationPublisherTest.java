package com.company.virs.notification;

import com.company.virs.config.secrets.SecretProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlackNotificationPublisherTest {

    @Mock
    private SecretProvider secretProvider;

    @InjectMocks
    private SlackNotificationPublisher publisher;

    @Test
    void publishNotification_ShouldReturnFalse_WhenWebhookMissing() {

        when(secretProvider.getSlackWebhookUrl())
                .thenReturn("");

        assertFalse(
                publisher.publishNotification(
                        "Message"
                )
        );
    }

    @Test
    void publishNotification_ShouldReturnFalse_WhenWebhookNull() {

        when(secretProvider.getSlackWebhookUrl())
                .thenReturn(null);

        assertFalse(
                publisher.publishNotification(
                        "Message"
                )
        );
    }
}