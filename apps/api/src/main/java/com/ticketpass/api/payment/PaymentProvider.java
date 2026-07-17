package com.ticketpass.api.payment;

public interface PaymentProvider {

    String providerName();

    String newProviderSessionId();

    PaymentSessionResult createSession(PaymentSessionRequest request);

    PaymentSessionResult getSession(String providerSessionId);

    void cancelSession(String providerSessionId);
}
