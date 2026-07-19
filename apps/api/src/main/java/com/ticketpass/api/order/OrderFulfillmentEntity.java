package com.ticketpass.api.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_fulfillments")
public class OrderFulfillmentEntity {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 40)
    private TransferStatus transferStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 40)
    private SettlementStatus settlementStatus;

    @Column(name = "transfer_deadline_at", nullable = false)
    private Instant transferDeadlineAt;

    @Column(name = "seller_confirmed_at")
    private Instant sellerConfirmedAt;

    @Column(name = "buyer_confirmed_at")
    private Instant buyerConfirmedAt;

    @Column(name = "settlement_released_at")
    private Instant settlementReleasedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getOrderId() { return orderId; }
    public OrderEntity getOrder() { return order; }
    public void setOrder(OrderEntity order) { this.order = order; }
    public TransferStatus getTransferStatus() { return transferStatus; }
    public void setTransferStatus(TransferStatus transferStatus) { this.transferStatus = transferStatus; }
    public SettlementStatus getSettlementStatus() { return settlementStatus; }
    public void setSettlementStatus(SettlementStatus settlementStatus) { this.settlementStatus = settlementStatus; }
    public Instant getTransferDeadlineAt() { return transferDeadlineAt; }
    public void setTransferDeadlineAt(Instant transferDeadlineAt) { this.transferDeadlineAt = transferDeadlineAt; }
    public Instant getSellerConfirmedAt() { return sellerConfirmedAt; }
    public void setSellerConfirmedAt(Instant sellerConfirmedAt) { this.sellerConfirmedAt = sellerConfirmedAt; }
    public Instant getBuyerConfirmedAt() { return buyerConfirmedAt; }
    public void setBuyerConfirmedAt(Instant buyerConfirmedAt) { this.buyerConfirmedAt = buyerConfirmedAt; }
    public Instant getSettlementReleasedAt() { return settlementReleasedAt; }
    public void setSettlementReleasedAt(Instant settlementReleasedAt) { this.settlementReleasedAt = settlementReleasedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
