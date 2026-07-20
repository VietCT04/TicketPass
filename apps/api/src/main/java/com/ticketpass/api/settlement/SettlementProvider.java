package com.ticketpass.api.settlement;
public interface SettlementProvider {
    String providerName();
    SettlementReleaseResult release(SettlementReleaseRequest request);
    SettlementReleaseResult lookup(SettlementReleaseRequest request);
}
