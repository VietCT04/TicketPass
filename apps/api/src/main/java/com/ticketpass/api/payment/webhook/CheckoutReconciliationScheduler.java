package com.ticketpass.api.payment.webhook;

import com.ticketpass.api.order.OrderRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class CheckoutReconciliationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutReconciliationScheduler.class);
    private final PaymentWebhookReceiptRepository receiptRepository;
    private final OrderRepository orderRepository;
    private final CheckoutReconciliationService reconciliationService;
    private final Clock clock;
    private final int batchSize;

    CheckoutReconciliationScheduler(
            PaymentWebhookReceiptRepository receiptRepository,
            OrderRepository orderRepository,
            CheckoutReconciliationService reconciliationService,
            Clock clock,
            @Value("${ticketpass.payments.reconciliation-batch-size:100}") int batchSize) {
        this.receiptRepository = receiptRepository;
        this.orderRepository = orderRepository;
        this.reconciliationService = reconciliationService;
        this.clock = clock;
        this.batchSize = Math.min(Math.max(batchSize, 1), 100);
    }

    @Scheduled(fixedDelayString = "${ticketpass.payments.reconciliation-interval-ms:5000}")
    void reconcile() {
        Instant now = clock.instant();
        for (UUID receiptId : receiptRepository.findDeferredCandidateIds(batchSize)) {
            try {
                LOGGER.info("Checkout reconciliation receipt {} result {}", receiptId,
                        reconciliationService.reconcileDeferredReceipt(receiptId, now));
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to reconcile payment receipt {}", receiptId);
            }
        }
        List<UUID> orderIds = orderRepository.findExpiredPendingOrderIds(now, PageRequest.of(0, batchSize));
        for (UUID orderId : orderIds) {
            try {
                LOGGER.info("Checkout reconciliation order {} result {}", orderId,
                        reconciliationService.reconcileExpiredOrder(orderId, now));
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to reconcile checkout order {}", orderId);
            }
        }
    }
}
