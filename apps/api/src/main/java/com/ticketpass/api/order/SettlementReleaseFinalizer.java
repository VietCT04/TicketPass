package com.ticketpass.api.order;

import com.ticketpass.api.audit.AuditAction;
import com.ticketpass.api.audit.AuditService;
import com.ticketpass.api.settlement.SettlementReleaseResult;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementReleaseFinalizer {

    private final OrderFulfillmentRepository fulfillmentRepository;
    private final SettlementReleaseOperationRepository operationRepository;
    private final AuditService auditService;
    private final Clock clock;

    public SettlementReleaseFinalizer(
            OrderFulfillmentRepository fulfillmentRepository,
            SettlementReleaseOperationRepository operationRepository,
            AuditService auditService,
            Clock clock) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.operationRepository = operationRepository;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public void finalizeResult(UUID orderId, SettlementReleaseResult result) {
        OrderFulfillmentEntity fulfillment = fulfillmentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException("Settlement fulfilment is missing"));
        SettlementReleaseOperationEntity operation = operationRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException("Settlement operation is missing"));
        if (operation.getStatus() == SettlementReleaseOperationStatus.SUCCEEDED) {
            return;
        }

        Instant now = clock.instant();
        operation.setProviderOperationId(result.providerOperationId());
        operation.setProcessingLeaseUntil(null);
        operation.setUpdatedAt(now);

        switch (result.outcome()) {
            case SUCCEEDED -> finalizeSuccess(orderId, fulfillment, operation, now);
            case PERMANENT_FAILURE -> finalizeReview(fulfillment, operation, result.errorCode(), now);
            case PENDING, RETRYABLE_FAILURE, UNKNOWN -> finalizeRetry(operation, result.errorCode(), now);
        }
    }

    private void finalizeSuccess(
            UUID orderId,
            OrderFulfillmentEntity fulfillment,
            SettlementReleaseOperationEntity operation,
            Instant now) {
        if (fulfillment.getTransferStatus() != TransferStatus.BUYER_CONFIRMED_RECEIPT
                || fulfillment.getBuyerConfirmedAt() == null
                || fulfillment.getSettlementStatus() != SettlementStatus.FUNDS_HELD) {
            throw new IllegalStateException("Settlement release state is inconsistent");
        }

        fulfillment.setSettlementStatus(SettlementStatus.RELEASED_TO_SELLER);
        fulfillment.setSettlementReleasedAt(now);
        fulfillment.setUpdatedAt(now);
        operation.setStatus(SettlementReleaseOperationStatus.SUCCEEDED);
        operation.setCompletedAt(now);
        operation.setNextAttemptAt(null);
        operation.setLastErrorCode(null);
        auditService.recordOrderAction(
                fulfillment.getOrder().getBuyerUserId(),
                orderId,
                AuditAction.SETTLEMENT_RELEASED,
                now);
    }

    private static void finalizeReview(
            OrderFulfillmentEntity fulfillment,
            SettlementReleaseOperationEntity operation,
            String errorCode,
            Instant now) {
        fulfillment.setSettlementStatus(SettlementStatus.REVIEW_REQUIRED);
        fulfillment.setUpdatedAt(now);
        operation.setStatus(SettlementReleaseOperationStatus.REQUIRES_REVIEW);
        operation.setLastErrorCode(errorCode);
    }

    private static void finalizeRetry(
            SettlementReleaseOperationEntity operation,
            String errorCode,
            Instant now) {
        operation.setStatus(SettlementReleaseOperationStatus.RETRYABLE_FAILURE);
        operation.setLastErrorCode(errorCode);
        operation.setNextAttemptAt(now.plusSeconds(30));
    }
}
