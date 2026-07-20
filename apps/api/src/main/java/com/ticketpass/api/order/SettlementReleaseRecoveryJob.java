package com.ticketpass.api.order;

import com.ticketpass.api.settlement.SettlementProvider;
import com.ticketpass.api.settlement.SettlementReleaseRequest;
import com.ticketpass.api.settlement.SettlementReleaseResult;
import java.time.Clock;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SettlementReleaseRecoveryJob {

    private static final int CLAIM_BATCH_SIZE = 20;

    private final SettlementReleaseOperationRepository operationRepository;
    private final SettlementReleaseClaimService claimService;
    private final OrderRepository orderRepository;
    private final SettlementProvider settlementProvider;
    private final SettlementReleaseFinalizer finalizer;
    private final Clock clock;

    public SettlementReleaseRecoveryJob(
            SettlementReleaseOperationRepository operationRepository,
            SettlementReleaseClaimService claimService,
            OrderRepository orderRepository,
            SettlementProvider settlementProvider,
            SettlementReleaseFinalizer finalizer,
            Clock clock) {
        this.operationRepository = operationRepository;
        this.claimService = claimService;
        this.orderRepository = orderRepository;
        this.settlementProvider = settlementProvider;
        this.finalizer = finalizer;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${ticketpass.settlement.recovery-interval-ms:5000}")
    public void recover() {
        for (UUID orderId : operationRepository.findClaimableOrderIds(
                clock.instant(), PageRequest.of(0, CLAIM_BATCH_SIZE))) {
            executeClaimedRelease(orderId);
        }
    }

    private void executeClaimedRelease(UUID orderId) {
        SettlementReleaseOperationEntity operation = claimService.claim(orderId);
        if (operation == null) {
            return;
        }

        OrderEntity order = orderRepository.findByIdForResponse(orderId).orElse(null);
        if (order == null) {
            return;
        }

        SettlementReleaseRequest request = new SettlementReleaseRequest(
                orderId,
                order.getAmountMinor(),
                order.getCurrency(),
                operation.getIdempotencyKey(),
                operation.getProviderOperationId());
        SettlementReleaseResult result = operation.getProviderOperationId() == null
                ? settlementProvider.release(request)
                : settlementProvider.lookup(request);
        finalizer.finalizeResult(orderId, result);
    }
}
