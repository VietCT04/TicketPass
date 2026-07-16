package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ListingReservationResponse(Reservation reservation) {

    static ListingReservationResponse from(ListingReservationEntity reservation) {
        return new ListingReservationResponse(new Reservation(
                reservation.getId().toString(),
                reservation.getListing().getId().toString(),
                reservation.getStatus(),
                reservation.getExpiresAt().toString()));
    }

    public record Reservation(
            String id,
            @JsonProperty("listing_id") String listingId,
            ListingReservationStatus status,
            @JsonProperty("expires_at") String expiresAt) {
    }
}
