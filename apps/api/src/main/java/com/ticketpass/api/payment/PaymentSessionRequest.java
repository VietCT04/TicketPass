package com.ticketpass.api.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentSessionRequest(
        UUID orderId,
        String providerSessionId,
        long amountMinor,
        String currency,
        Instant expiresAt) {
}
