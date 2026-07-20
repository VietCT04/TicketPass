package com.ticketpass.api.settlement;
import java.util.UUID;
public record SettlementReleaseRequest(UUID orderId, long amountMinor, String currency, String idempotencyKey, String providerOperationId) {}
