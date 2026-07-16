package com.ticketpass.api.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticketpass.api.order.OrderEntity;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CheckoutResponse(
        Order order,
        @JsonProperty("payment_url") String paymentUrl,
        @JsonProperty("payment_url_expires_at") Instant paymentUrlExpiresAt) {

    static CheckoutResponse from(OrderEntity order, String paymentUrl, Instant paymentUrlExpiresAt) {
        return new CheckoutResponse(new Order(
                order.getId().toString(),
                order.getReservation().getId().toString(),
                order.getListing().getId().toString(),
                order.getStatus().name(),
                order.getAmountMinor(),
                order.getCurrency(),
                order.getExpiresAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()), paymentUrl, paymentUrlExpiresAt);
    }

    public record Order(
            String id,
            @JsonProperty("reservation_id") String reservationId,
            @JsonProperty("listing_id") String listingId,
            String status,
            @JsonProperty("amount_minor") long amountMinor,
            String currency,
            @JsonProperty("expires_at") Instant expiresAt,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("updated_at") Instant updatedAt) {
    }
}
