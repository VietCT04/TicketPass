package com.ticketpass.api.payment;

public class PaymentProviderUnavailableException extends RuntimeException {

    public PaymentProviderUnavailableException() {
        super("Checkout provider is temporarily unavailable");
    }
}
