package com.ticketpass.api.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticketpass.api.order.SafeOrderResponse;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CheckoutResponse(
        SafeOrderResponse order,
        @JsonProperty("payment_url") String paymentUrl,
        @JsonProperty("payment_url_expires_at") Instant paymentUrlExpiresAt) {
}
