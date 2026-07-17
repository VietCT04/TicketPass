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
import com.ticketpass.api.payment.PaymentProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
class MockPaymentDeliveryService {

    private static final String PENDING = "PENDING";
    private static final String DELIVERED = "DELIVERED";
    private static final String DEAD_LETTER = "DEAD_LETTER";
    private static final Duration DELIVERY_LEASE = Duration.ofSeconds(30);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration[] RETRY_DELAYS = {
            Duration.ofSeconds(5), Duration.ofSeconds(15), Duration.ofSeconds(30),
            Duration.ofMinutes(1), Duration.ofMinutes(2), Duration.ofMinutes(5), Duration.ofMinutes(10)};
    private final MockPaymentEventRepository eventRepository;
    private final MockProviderSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final URI webhookUrl;
    private final byte[] webhookSecret;
    private final TransactionTemplate transactionTemplate;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    MockPaymentDeliveryService(
            MockPaymentEventRepository eventRepository,
            MockProviderSessionRepository sessionRepository,
            ObjectMapper objectMapper,
            Clock clock,
            PaymentProperties paymentProperties,
            PlatformTransactionManager transactionManager) {
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.webhookUrl = paymentProperties.mock().webhookUrl();
        this.webhookSecret = paymentProperties.mock().webhookSecret().getBytes(StandardCharsets.UTF_8);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    DeliveryAttempt claim(UUID eventId, Instant now) {
        MockPaymentEventEntity event = eventRepository.findByIdForDelivery(eventId).orElse(null);
        if (event == null || !PENDING.equals(event.getDeliveryStatus()) || event.getNextAttemptAt().isAfter(now)) {
            return null;
        }
        Instant leaseUntil = now.plus(DELIVERY_LEASE);
        event.setAttemptCount(event.getAttemptCount() + 1);
        event.setLastAttemptAt(now);
        event.setNextAttemptAt(leaseUntil);
        MockProviderSessionEntity session = sessionRepository.findByProviderSessionId(event.getProviderSessionId()).orElse(null);
        if (session == null) {
            return new DeliveryAttempt(event.getId(), leaseUntil, null, null);
        }
        try {
            byte[] body = objectMapper.writeValueAsBytes(new Payload(
                    event.getId().toString(), event.getEventType().name(), session.getProviderSessionId(),
                    session.getAmountMinor(), session.getCurrency(), event.getCreatedAt()));
            String timestamp = Long.toString(now.getEpochSecond());
            return new DeliveryAttempt(event.getId(), leaseUntil, body, timestamp);
        } catch (Exception exception) {
            return new DeliveryAttempt(event.getId(), leaseUntil, null, null);
        }
    }

    DeliveryResult deliver(DeliveryAttempt attempt) {
        if (attempt.body() == null) {
            return transactionTemplate.execute(status -> finalizeAttempt(attempt, DeliveryOutcome.RETRY, clock.instant()));
        }
        try {
            int status = httpClient.send(HttpRequest.newBuilder(webhookUrl)
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("X-Mock-Timestamp", attempt.timestamp())
                    .header("X-Mock-Signature", "v1=" + signature(attempt.timestamp(), attempt.body()))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(attempt.body()))
                    .build(), HttpResponse.BodyHandlers.discarding()).statusCode();
            DeliveryOutcome outcome = status >= 200 && status < 300 ? DeliveryOutcome.DELIVERED
                    : status >= 500 ? DeliveryOutcome.RETRY : DeliveryOutcome.DEAD_LETTER;
            return transactionTemplate.execute(transaction -> finalizeAttempt(attempt, outcome, clock.instant()));
        } catch (Exception exception) {
            return transactionTemplate.execute(status -> finalizeAttempt(attempt, DeliveryOutcome.RETRY, clock.instant()));
        }
    }

    private DeliveryResult finalizeAttempt(DeliveryAttempt attempt, DeliveryOutcome outcome, Instant now) {
        MockPaymentEventEntity event = eventRepository.findByIdForDelivery(attempt.eventId()).orElse(null);
        if (event == null || !PENDING.equals(event.getDeliveryStatus()) || !attempt.leaseUntil().equals(event.getNextAttemptAt())) {
            return DeliveryResult.SKIPPED;
        }
        if (outcome == DeliveryOutcome.DELIVERED) {
            event.setDeliveryStatus(DELIVERED);
            event.setDeliveredAt(now);
            return DeliveryResult.DELIVERED;
        }
        if (outcome == DeliveryOutcome.DEAD_LETTER || event.getAttemptCount() >= 8) {
            event.setDeliveryStatus(DEAD_LETTER);
            return DeliveryResult.DEAD_LETTER;
        } else {
            event.setNextAttemptAt(now.plus(RETRY_DELAYS[Math.min(event.getAttemptCount() - 1, RETRY_DELAYS.length - 1)]));
            return DeliveryResult.RETRY_SCHEDULED;
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

    record DeliveryAttempt(UUID eventId, Instant leaseUntil, byte[] body, String timestamp) { }

    enum DeliveryResult { SKIPPED, DELIVERED, RETRY_SCHEDULED, DEAD_LETTER }

    private enum DeliveryOutcome { DELIVERED, RETRY, DEAD_LETTER }
}
