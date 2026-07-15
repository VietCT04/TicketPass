package com.ticketpass.api.listing;

import java.util.UUID;

public record EventListingSummaryRow(
        UUID id,
        String ticketType,
        String seatInfo,
        String eventPlatform,
        long askingPriceMinor,
        String currency,
        TransferMethod transferMethod) {
}
