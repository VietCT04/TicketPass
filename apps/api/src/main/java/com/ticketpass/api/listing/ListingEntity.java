package com.ticketpass.api.listing;

import com.ticketpass.api.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listings")
public class ListingEntity {

    public static final int MVP_QUANTITY = 1;
    public static final String MVP_CURRENCY = "VND";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private UserEntity seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @Column(name = "event_platform", nullable = false, length = 120)
    private String eventPlatform;

    @Column(name = "seat_info", nullable = false, length = 255)
    private String seatInfo;

    @Column(name = "ticket_type", nullable = false, length = 120)
    private String ticketType;

    @Column(nullable = false)
    private int quantity = MVP_QUANTITY;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "asking_price_minor", nullable = false)
    private long askingPriceMinor;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_method", nullable = false, length = 40)
    private TransferMethod transferMethod;

    @Column(name = "is_transferable_confirmed", nullable = false)
    private boolean transferableConfirmed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ListingStatus status;

    @Column(name = "public_notes", columnDefinition = "text")
    private String publicNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getSeller() {
        return seller;
    }

    public void setSeller(UserEntity seller) {
        this.seller = seller;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }

    public String getEventPlatform() {
        return eventPlatform;
    }

    public void setEventPlatform(String eventPlatform) {
        this.eventPlatform = eventPlatform;
    }

    public String getSeatInfo() {
        return seatInfo;
    }

    public void setSeatInfo(String seatInfo) {
        this.seatInfo = seatInfo;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getAskingPriceMinor() {
        return askingPriceMinor;
    }

    public void setAskingPriceMinor(long askingPriceMinor) {
        this.askingPriceMinor = askingPriceMinor;
    }

    public TransferMethod getTransferMethod() {
        return transferMethod;
    }

    public void setTransferMethod(TransferMethod transferMethod) {
        this.transferMethod = transferMethod;
    }

    public boolean isTransferableConfirmed() {
        return transferableConfirmed;
    }

    public void setTransferableConfirmed(boolean transferableConfirmed) {
        this.transferableConfirmed = transferableConfirmed;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public String getPublicNotes() {
        return publicNotes;
    }

    public void setPublicNotes(String publicNotes) {
        this.publicNotes = publicNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

