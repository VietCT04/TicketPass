package com.ticketpass.api.listing;

import java.time.Instant;
import java.util.UUID;

public record EventBrowseRow(
        UUID id,
        String name,
        Instant startsAt,
        String venue,
        String city,
        Long lowestPriceMinor,
        Long availableListingCount) {
}
