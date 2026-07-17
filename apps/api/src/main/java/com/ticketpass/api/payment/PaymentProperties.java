package com.ticketpass.api.payment;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ticketpass.payments")
public record PaymentProperties(
        String provider,
        URI frontendBaseUrl,
        int reconciliationIntervalMs,
        int reconciliationBatchSize,
        Mock mock) {

    public record Mock(
            boolean enabled,
            URI providerBaseUrl,
            URI webhookUrl,
            String webhookSecret,
            boolean allowNonLoopback,
            int deliveryIntervalMs) {
    }
}
