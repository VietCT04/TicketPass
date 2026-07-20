package com.ticketpass.api.order;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementReleaseClaimService {

    private static final Duration PROCESSING_LEASE = Duration.ofMinutes(2);

    private final SettlementReleaseOperationRepository operationRepository;
    private final Clock clock;

    public SettlementReleaseClaimService(
            SettlementReleaseOperationRepository operationRepository,
            Clock clock) {
        this.operationRepository = operationRepository;
        this.clock = clock;
    }

    @Transactional
    public SettlementReleaseOperationEntity claim(UUID orderId) {
        SettlementReleaseOperationEntity operation = operationRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException("Settlement operation is missing"));
        Instant now = clock.instant();
        if (!isClaimable(operation, now)) {
            return null;
        }

        operation.setStatus(SettlementReleaseOperationStatus.PROCESSING);
        operation.setAttemptCount(operation.getAttemptCount() + 1);
        operation.setProcessingLeaseUntil(now.plus(PROCESSING_LEASE));
        operation.setUpdatedAt(now);
        return operation;
    }

    private static boolean isClaimable(SettlementReleaseOperationEntity operation, Instant now) {
        return switch (operation.getStatus()) {
            case PENDING -> true;
            case RETRYABLE_FAILURE -> operation.getNextAttemptAt() == null
                    || !operation.getNextAttemptAt().isAfter(now);
            case PROCESSING -> operation.getProcessingLeaseUntil() != null
                    && !operation.getProcessingLeaseUntil().isAfter(now);
            case SUCCEEDED, REQUIRES_REVIEW -> false;
        };
    }
}
