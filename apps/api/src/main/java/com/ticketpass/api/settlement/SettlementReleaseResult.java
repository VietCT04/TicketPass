package com.ticketpass.api.settlement;
public record SettlementReleaseResult(Outcome outcome, String providerOperationId, String errorCode) {
    public enum Outcome { SUCCEEDED, PENDING, RETRYABLE_FAILURE, PERMANENT_FAILURE, UNKNOWN }
}
