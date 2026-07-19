package com.ticketpass.api.order;

import java.util.UUID;

public record SellerTransferLockTarget(UUID listingId, UUID reservationId) {
}
