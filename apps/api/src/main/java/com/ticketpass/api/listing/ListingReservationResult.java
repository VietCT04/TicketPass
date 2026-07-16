package com.ticketpass.api.listing;

public record ListingReservationResult(
        ListingReservationEntity reservation,
        boolean created) {
}
