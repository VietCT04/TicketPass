package com.ticketpass.api.listing;

import java.time.Instant;
import java.util.UUID;

public record SellerOwnListingRow(
        UUID id,
        ListingStatus status,
        String eventPlatform,
        String seatInfo,
        String ticketType,
        int quantity,
        long askingPriceMinor,
        String currency,
        TransferMethod transferMethod,
        boolean transferableConfirmed,
        String publicNotes,
        Instant createdAt,
        Instant updatedAt,
        UUID eventId,
        String eventName,
        Instant eventStartsAt,
        String eventVenue,
        String eventCity) {
}
