package com.ticketpass.api.payment.webhook;

import java.time.Instant;

record MockWebhookPayload(
        String eventId,
        String eventType,
        String providerSessionId,
        long amountMinor,
        String currency,
        Instant occurredAt) {
}
