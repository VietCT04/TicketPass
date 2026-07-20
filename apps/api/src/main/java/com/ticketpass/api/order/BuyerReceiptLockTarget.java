package com.ticketpass.api.order;

import java.util.UUID;

public record BuyerReceiptLockTarget(UUID listingId, UUID reservationId) {
}
