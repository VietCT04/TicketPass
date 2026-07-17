package com.ticketpass.api.payment.mock;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class MockPaymentDeliveryScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockPaymentDeliveryScheduler.class);
    private final MockPaymentEventRepository eventRepository;
    private final MockPaymentDeliveryService deliveryService;
    private final Clock clock;

    MockPaymentDeliveryScheduler(MockPaymentEventRepository eventRepository, MockPaymentDeliveryService deliveryService, Clock clock) {
        this.eventRepository = eventRepository;
        this.deliveryService = deliveryService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${ticketpass.payments.mock.delivery-interval-ms:5000}")
    void deliverPendingEvents() {
        Instant now = clock.instant();
        List<MockPaymentEventEntity> events = eventRepository.findDeliveryCandidates("PENDING", now, PageRequest.of(0, 100));
        for (MockPaymentEventEntity event : events) {
            try {
                MockPaymentDeliveryService.DeliveryAttempt attempt = deliveryService.claim(event.getId(), now);
                if (attempt == null) continue;
                MockPaymentDeliveryService.DeliveryResult result = deliveryService.deliver(attempt);
                if (result == MockPaymentDeliveryService.DeliveryResult.DEAD_LETTER) {
                    LOGGER.error("Mock payment event {} reached dead letter", event.getId());
                } else if (result != MockPaymentDeliveryService.DeliveryResult.SKIPPED) {
                    LOGGER.info("Mock payment event {} delivery {}", event.getId(), result);
                }
            } catch (RuntimeException exception) {
                LOGGER.error("Mock payment event {} delivery failed with {}", event.getId(), exception.getClass().getSimpleName());
            }
        }
    }
}
