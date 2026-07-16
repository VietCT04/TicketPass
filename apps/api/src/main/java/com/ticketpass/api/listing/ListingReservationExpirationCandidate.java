package com.ticketpass.api.listing;

import java.util.UUID;

public record ListingReservationExpirationCandidate(
        UUID reservationId,
        UUID listingId) {
}
