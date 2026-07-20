package com.ticketpass.api.order;
import java.time.*;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SettlementReleaseClaimService {
 private final SettlementReleaseOperationRepository operations; private final Clock clock;
 public SettlementReleaseClaimService(SettlementReleaseOperationRepository operations, Clock clock){this.operations=operations;this.clock=clock;}
 @Transactional public SettlementReleaseOperationEntity claim(UUID orderId){SettlementReleaseOperationEntity operation=operations.findByOrderIdForUpdate(orderId).orElseThrow(); Instant now=clock.instant(); if(operation.getStatus()==SettlementReleaseOperationStatus.SUCCEEDED || operation.getStatus()==SettlementReleaseOperationStatus.REQUIRES_REVIEW || (operation.getStatus()==SettlementReleaseOperationStatus.PROCESSING && operation.getProcessingLeaseUntil().isAfter(now)))return null; if(operation.getStatus()==SettlementReleaseOperationStatus.RETRYABLE_FAILURE && operation.getNextAttemptAt()!=null && operation.getNextAttemptAt().isAfter(now))return null; operation.setStatus(SettlementReleaseOperationStatus.PROCESSING);operation.setAttemptCount(operation.getAttemptCount()+1);operation.setProcessingLeaseUntil(now.plus(Duration.ofMinutes(2)));operation.setUpdatedAt(now);return operation;}
}
