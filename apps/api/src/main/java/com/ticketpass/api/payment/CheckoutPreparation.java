package com.ticketpass.api.payment;

import com.ticketpass.api.order.OrderEntity;

record CheckoutPreparation(
        OrderEntity order,
        PaymentSessionEntity paymentSession,
        boolean orderCreated,
        boolean paymentSessionCreated) {
}
