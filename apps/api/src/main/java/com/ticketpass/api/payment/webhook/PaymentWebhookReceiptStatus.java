package com.ticketpass.api.payment.webhook;

public enum PaymentWebhookReceiptStatus {
    PROCESSED,
    DEFERRED,
    REQUIRES_ACTION,
    IGNORED
}
