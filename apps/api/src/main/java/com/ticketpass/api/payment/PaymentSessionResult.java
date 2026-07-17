package com.ticketpass.api.payment;

import java.time.Instant;

public record PaymentSessionResult(
        String providerSessionId,
        PaymentSessionStatus status,
        long amountMinor,
        String currency,
        Instant expiresAt,
        String hostedCheckoutUrl) {
}
