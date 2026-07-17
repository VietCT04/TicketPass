package com.ticketpass.api.payment.mock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mock_payment_events")
public class MockPaymentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "provider_session_id", nullable = false, length = 120)
    private String providerSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private MockPaymentEventType eventType;

    @Column(name = "delivery_status", nullable = false, length = 40)
    private String deliveryStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    public void setProviderSessionId(String providerSessionId) { this.providerSessionId = providerSessionId; }
    public void setEventType(MockPaymentEventType eventType) { this.eventType = eventType; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
