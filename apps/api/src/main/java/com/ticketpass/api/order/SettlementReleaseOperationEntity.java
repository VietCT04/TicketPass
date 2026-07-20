package com.ticketpass.api.order;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlement_release_operations")
public class SettlementReleaseOperationEntity {
    @Id @Column(name = "order_id") private UUID orderId;
    @MapsId @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id") private OrderEntity order;
    @Column(nullable = false, length = 40) private String provider;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 160) private String idempotencyKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private SettlementReleaseOperationStatus status;
    @Column(name = "provider_operation_id", length = 255) private String providerOperationId;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Column(name = "next_attempt_at") private Instant nextAttemptAt;
    @Column(name = "processing_lease_until") private Instant processingLeaseUntil;
    @Column(name = "last_error_code", length = 80) private String lastErrorCode;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "completed_at") private Instant completedAt;
    public UUID getOrderId(){return orderId;} public void setOrder(OrderEntity v){order=v;} public OrderEntity getOrder(){return order;}
    public String getProvider(){return provider;} public void setProvider(String v){provider=v;} public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;}
    public SettlementReleaseOperationStatus getStatus(){return status;} public void setStatus(SettlementReleaseOperationStatus v){status=v;} public String getProviderOperationId(){return providerOperationId;} public void setProviderOperationId(String v){providerOperationId=v;}
    public int getAttemptCount(){return attemptCount;} public void setAttemptCount(int v){attemptCount=v;} public Instant getNextAttemptAt(){return nextAttemptAt;} public void setNextAttemptAt(Instant v){nextAttemptAt=v;}
    public Instant getProcessingLeaseUntil(){return processingLeaseUntil;} public void setProcessingLeaseUntil(Instant v){processingLeaseUntil=v;} public String getLastErrorCode(){return lastErrorCode;} public void setLastErrorCode(String v){lastErrorCode=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;} public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;} public Instant getCompletedAt(){return completedAt;} public void setCompletedAt(Instant v){completedAt=v;}
}
