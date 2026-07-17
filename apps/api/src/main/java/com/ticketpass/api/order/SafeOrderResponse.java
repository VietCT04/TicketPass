package com.ticketpass.api.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticketpass.api.listing.ListingEntity;
import java.time.Instant;

public record SafeOrderResponse(
        String id,
        @JsonProperty("reservation_id") String reservationId,
        @JsonProperty("listing_id") String listingId,
        String status,
        @JsonProperty("amount_minor") long amountMinor,
        String currency,
        @JsonProperty("expires_at") Instant expiresAt,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("paid_at") Instant paidAt,
        @JsonProperty("payment_review_required") boolean paymentReviewRequired,
        Event event,
        Ticket ticket) {

    public static SafeOrderResponse from(OrderEntity order, boolean paymentReviewRequired) {
        ListingEntity listing = order.getListing();
        return new SafeOrderResponse(
                order.getId().toString(),
                order.getReservation().getId().toString(),
                listing.getId().toString(),
                order.getStatus().name(),
                order.getAmountMinor(),
                order.getCurrency(),
                order.getExpiresAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getPaidAt(),
                paymentReviewRequired,
                new Event(listing.getEvent().getName(), listing.getEvent().getStartsAt(),
                        listing.getEvent().getVenue(), listing.getEvent().getCity()),
                new Ticket(listing.getTicketType(), listing.getSeatInfo(), listing.getTransferMethod().name()));
    }

    public record Event(
            String name,
            @JsonProperty("starts_at") Instant startsAt,
            String venue,
            String city) {
    }

    public record Ticket(
            @JsonProperty("ticket_type") String ticketType,
            @JsonProperty("seat_info") String seatInfo,
            @JsonProperty("transfer_method") String transferMethod) {
    }
}
