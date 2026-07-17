package com.ticketpass.api.payment.mock;

import com.ticketpass.api.payment.PaymentProvider;
import com.ticketpass.api.payment.PaymentProviderUnavailableException;
import com.ticketpass.api.payment.PaymentSessionRequest;
import com.ticketpass.api.payment.PaymentSessionResult;
import com.ticketpass.api.payment.PaymentSessionStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "ticketpass.payments.provider", havingValue = "mock")
public class MockPaymentProvider implements PaymentProvider {

    public static final String PROVIDER = "MOCK";
    private static final String PENDING_DELIVERY = "PENDING";

    private final MockProviderSessionRepository sessionRepository;
    private final MockPaymentEventRepository eventRepository;
    private final Clock clock;
    private final String providerBaseUrl;

    public MockPaymentProvider(
            MockProviderSessionRepository sessionRepository,
            MockPaymentEventRepository eventRepository,
            Clock clock,
            @Value("${ticketpass.payments.mock.provider-base-url:http://localhost:8080}") String providerBaseUrl) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.clock = clock;
        this.providerBaseUrl = stripTrailingSlash(providerBaseUrl);
    }

    @Override
    public String providerName() {
        return PROVIDER;
    }

    @Override
    public String newProviderSessionId() {
        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    public PaymentSessionResult createSession(PaymentSessionRequest request) {
        MockProviderSessionEntity existing = sessionRepository
                .findByProviderSessionIdForUpdate(request.providerSessionId())
                .orElse(null);
        if (existing != null) {
            return toResult(expireIfNeeded(existing, clock.instant()));
        }

        Instant now = clock.instant();
        if (!request.expiresAt().isAfter(now)) {
            throw new PaymentProviderUnavailableException();
        }

        MockProviderSessionEntity session = new MockProviderSessionEntity();
        session.setProviderSessionId(request.providerSessionId());
        session.setOrderId(request.orderId());
        session.setAmountMinor(request.amountMinor());
        session.setCurrency(request.currency());
        session.setStatus(MockProviderSessionStatus.PENDING);
        session.setExpiresAt(request.expiresAt());
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        return toResult(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public PaymentSessionResult getSession(String providerSessionId) {
        MockProviderSessionEntity session = sessionRepository.findByProviderSessionIdForUpdate(providerSessionId)
                .orElseThrow(MockProviderSessionNotFoundException::new);
        return toResult(expireIfNeeded(session, clock.instant()));
    }

    @Override
    @Transactional
    public void cancelSession(String providerSessionId) {
        transition(providerSessionId, MockProviderSessionStatus.CANCELLED);
    }

    @Transactional
    public PaymentSessionResult transition(String providerSessionId, MockProviderSessionStatus targetStatus) {
        MockProviderSessionEntity session = sessionRepository.findByProviderSessionIdForUpdate(providerSessionId)
                .orElseThrow(() -> new MockProviderSessionNotFoundException());
        Instant now = clock.instant();
        expireIfNeeded(session, now);
        if (session.getStatus() == targetStatus || isTerminal(session.getStatus())) {
            return toResult(session);
        }
        if (session.getStatus() != MockProviderSessionStatus.PENDING) {
            return toResult(session);
        }

        session.setStatus(targetStatus);
        session.setUpdatedAt(now);
        createEvent(session.getProviderSessionId(), eventFor(targetStatus), now);
        return toResult(session);
    }

    @Transactional(readOnly = true)
    public java.util.UUID orderIdForRedirect(String providerSessionId) {
        return sessionRepository.findByProviderSessionId(providerSessionId)
                .orElseThrow(MockProviderSessionNotFoundException::new)
                .getOrderId();
    }

    private MockProviderSessionEntity expireIfNeeded(MockProviderSessionEntity session, Instant now) {
        if (session.getStatus() == MockProviderSessionStatus.PENDING && !session.getExpiresAt().isAfter(now)) {
            session.setStatus(MockProviderSessionStatus.EXPIRED);
            session.setUpdatedAt(now);
        }
        return session;
    }

    private void createEvent(String providerSessionId, MockPaymentEventType eventType, Instant now) {
        MockPaymentEventEntity event = new MockPaymentEventEntity();
        event.setProviderSessionId(providerSessionId);
        event.setEventType(eventType);
        event.setDeliveryStatus(PENDING_DELIVERY);
        event.setCreatedAt(now);
        eventRepository.save(event);
    }

    private PaymentSessionResult toResult(MockProviderSessionEntity session) {
        return new PaymentSessionResult(
                session.getProviderSessionId(),
                PaymentSessionStatus.valueOf(session.getStatus().name()),
                session.getAmountMinor(),
                session.getCurrency(),
                session.getExpiresAt(),
                session.getStatus() == MockProviderSessionStatus.PENDING
                        ? providerBaseUrl + "/mock-provider/checkout/" + session.getProviderSessionId()
                        : null);
    }

    private static MockPaymentEventType eventFor(MockProviderSessionStatus status) {
        return switch (status) {
            case PAID -> MockPaymentEventType.PAYMENT_SUCCEEDED;
            case FAILED -> MockPaymentEventType.PAYMENT_FAILED;
            case CANCELLED -> MockPaymentEventType.PAYMENT_CANCELLED;
            default -> throw new IllegalArgumentException("No event for status");
        };
    }

    private static boolean isTerminal(MockProviderSessionStatus status) {
        return status != MockProviderSessionStatus.PENDING;
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
