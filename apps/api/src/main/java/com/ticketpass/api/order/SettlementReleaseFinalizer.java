package com.ticketpass.api.order;
import com.ticketpass.api.audit.*;
import com.ticketpass.api.settlement.SettlementReleaseResult;
import java.time.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SettlementReleaseFinalizer {
 private final OrderFulfillmentRepository fulfillments; private final SettlementReleaseOperationRepository operations; private final AuditService audit; private final Clock clock;
 public SettlementReleaseFinalizer(OrderFulfillmentRepository f, SettlementReleaseOperationRepository o, AuditService a, Clock c){fulfillments=f;operations=o;audit=a;clock=c;}
 @Transactional public void finalizeResult(UUID orderId, SettlementReleaseResult result){
  OrderFulfillmentEntity f=fulfillments.findByOrderIdForUpdate(orderId).orElseThrow(); SettlementReleaseOperationEntity o=operations.findByOrderIdForUpdate(orderId).orElseThrow(); if(o.getStatus()==SettlementReleaseOperationStatus.SUCCEEDED)return; Instant now=clock.instant(); o.setProviderOperationId(result.providerOperationId()); o.setProcessingLeaseUntil(null); o.setUpdatedAt(now);
  if(result.outcome()==SettlementReleaseResult.Outcome.SUCCEEDED){if(f.getTransferStatus()!=TransferStatus.BUYER_CONFIRMED_RECEIPT || f.getBuyerConfirmedAt()==null || f.getSettlementStatus()!=SettlementStatus.FUNDS_HELD) throw new IllegalStateException("Settlement release state is inconsistent"); f.setSettlementStatus(SettlementStatus.RELEASED_TO_SELLER);f.setSettlementReleasedAt(now);f.setUpdatedAt(now);o.setStatus(SettlementReleaseOperationStatus.SUCCEEDED);o.setCompletedAt(now);o.setNextAttemptAt(null);o.setLastErrorCode(null);audit.recordOrderAction(f.getOrder().getBuyerUserId(),orderId,AuditAction.SETTLEMENT_RELEASED,now);return;}
  if(result.outcome()==SettlementReleaseResult.Outcome.PERMANENT_FAILURE){f.setSettlementStatus(SettlementStatus.REVIEW_REQUIRED);f.setUpdatedAt(now);o.setStatus(SettlementReleaseOperationStatus.REQUIRES_REVIEW);o.setLastErrorCode(result.errorCode());return;}
  o.setStatus(SettlementReleaseOperationStatus.RETRYABLE_FAILURE);o.setLastErrorCode(result.errorCode());o.setNextAttemptAt(now.plusSeconds(30));
 }
}
