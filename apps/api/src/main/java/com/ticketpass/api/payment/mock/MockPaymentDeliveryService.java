package com.ticketpass.api.payment.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MockPaymentDeliveryService {

    private static final String PENDING = "PENDING";
    private static final String DELIVERED = "DELIVERED";
    private static final String DEAD_LETTER = "DEAD_LETTER";
    private static final Duration[] RETRY_DELAYS = {
            Duration.ofSeconds(5), Duration.ofSeconds(15), Duration.ofSeconds(30),
            Duration.ofMinutes(1), Duration.ofMinutes(2), Duration.ofMinutes(5), Duration.ofMinutes(10)};
    private final MockPaymentEventRepository eventRepository;
    private final MockProviderSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String webhookUrl;
    private final byte[] webhookSecret;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    MockPaymentDeliveryService(
            MockPaymentEventRepository eventRepository,
            MockProviderSessionRepository sessionRepository,
            ObjectMapper objectMapper,
            Clock clock,
            @Value("${ticketpass.payments.mock.webhook-url}") String webhookUrl,
            @Value("${ticketpass.payments.mock.webhook-secret}") String webhookSecret) {
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    void deliver(MockPaymentEventEntity event, Instant now) {
        if (!PENDING.equals(event.getDeliveryStatus()) || event.getNextAttemptAt().isAfter(now)) return;
        MockProviderSessionEntity session = sessionRepository.findByProviderSessionId(event.getProviderSessionId()).orElse(null);
        if (session == null) {
            failure(event, now);
            eventRepository.save(event);
            return;
        }
        try {
            byte[] body = objectMapper.writeValueAsBytes(new Payload(
                    event.getId().toString(), event.getEventType().name(), session.getProviderSessionId(),
                    session.getAmountMinor(), session.getCurrency(), event.getCreatedAt()));
            String timestamp = Long.toString(now.getEpochSecond());
            int status = httpClient.send(HttpRequest.newBuilder(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .header("X-Mock-Timestamp", timestamp)
                    .header("X-Mock-Signature", "v1=" + signature(timestamp, body))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build(), HttpResponse.BodyHandlers.discarding()).statusCode();
            if (status >= 200 && status < 300) {
                event.setDeliveryStatus(DELIVERED);
                event.setDeliveredAt(now);
                event.setLastAttemptAt(now);
                event.setAttemptCount(event.getAttemptCount() + 1);
            } else if (status >= 500) {
                failure(event, now);
            } else {
                event.setAttemptCount(event.getAttemptCount() + 1);
                event.setLastAttemptAt(now);
                event.setDeliveryStatus(DEAD_LETTER);
            }
        } catch (Exception exception) {
            failure(event, now);
        }
        eventRepository.save(event);
    }

    private void failure(MockPaymentEventEntity event, Instant now) {
        int attempts = event.getAttemptCount() + 1;
        event.setAttemptCount(attempts);
        event.setLastAttemptAt(now);
        if (attempts >= 8) {
            event.setDeliveryStatus(DEAD_LETTER);
        } else {
            event.setNextAttemptAt(now.plus(RETRY_DELAYS[Math.min(attempts - 1, RETRY_DELAYS.length - 1)]));
        }
    }

    private String signature(String timestamp, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(webhookSecret, "HmacSHA256"));
        mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
        byte[] bytes = mac.doFinal(body);
        StringBuilder result = new StringBuilder(64);
        for (byte value : bytes) result.append(String.format("%02x", value));
        return result.toString();
    }

    private record Payload(
            String event_id, String event_type, String provider_session_id,
            long amount_minor, String currency, Instant occurred_at) { }
}
