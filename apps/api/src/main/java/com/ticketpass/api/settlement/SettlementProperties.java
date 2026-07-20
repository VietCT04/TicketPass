package com.ticketpass.api.settlement;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "ticketpass.settlement")
public record SettlementProperties(String provider, boolean enabled, long processingLeaseMs, long initialRetryDelayMs, long maximumRetryDelayMs, int maximumAttempts, Mock mock) {
    public record Mock(boolean enabled, String outcome) {}
}
