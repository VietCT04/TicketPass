package com.ticketpass.api.order;

import com.ticketpass.api.settlement.*;
import java.time.Clock;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SettlementReleaseRecoveryJob {
    private final SettlementReleaseOperationRepository operations;
    private final SettlementReleaseClaimService claims;
    private final OrderRepository orders;
    private final SettlementProvider provider;
    private final SettlementReleaseFinalizer finalizer;
    private final Clock clock;
    public SettlementReleaseRecoveryJob(SettlementReleaseOperationRepository operations, SettlementReleaseClaimService claims, OrderRepository orders, SettlementProvider provider, SettlementReleaseFinalizer finalizer, Clock clock) { this.operations=operations;this.claims=claims;this.orders=orders;this.provider=provider;this.finalizer=finalizer;this.clock=clock; }
    @Scheduled(fixedDelayString = "${ticketpass.settlement.recovery-interval-ms:5000}")
    public void recover() { for (UUID orderId : operations.findClaimableOrderIds(clock.instant(), PageRequest.of(0, 20))) { SettlementReleaseOperationEntity operation=claims.claim(orderId); if (operation==null) continue; OrderEntity order=orders.findByIdForResponse(orderId).orElse(null); if(order==null) continue; SettlementReleaseResult result=operation.getProviderOperationId()==null ? provider.release(new SettlementReleaseRequest(orderId,order.getAmountMinor(),order.getCurrency(),operation.getIdempotencyKey(),null)) : provider.lookup(new SettlementReleaseRequest(orderId,order.getAmountMinor(),order.getCurrency(),operation.getIdempotencyKey(),operation.getProviderOperationId())); finalizer.finalizeResult(orderId,result); } }
}
