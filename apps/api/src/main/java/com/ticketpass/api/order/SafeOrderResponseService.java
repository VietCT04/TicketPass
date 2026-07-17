package com.ticketpass.api.order;

import com.ticketpass.api.payment.PaymentSessionEntity;
import com.ticketpass.api.payment.PaymentSessionRepository;
import com.ticketpass.api.payment.webhook.PaymentWebhookReceiptRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafeOrderResponseService {

    private final OrderRepository orderRepository;
    private final PaymentSessionRepository paymentSessionRepository;
    private final PaymentWebhookReceiptRepository receiptRepository;

    public SafeOrderResponseService(
            OrderRepository orderRepository,
            PaymentSessionRepository paymentSessionRepository,
            PaymentWebhookReceiptRepository receiptRepository) {
        this.orderRepository = orderRepository;
        this.paymentSessionRepository = paymentSessionRepository;
        this.receiptRepository = receiptRepository;
    }

    @Transactional(readOnly = true)
    public SafeOrderResponse forCheckout(UUID orderId) {
        OrderEntity order = orderRepository.findByIdForResponse(orderId).orElseThrow();
        return response(order);
    }

    @Transactional(readOnly = true)
    public SafeOrderResponse forBuyer(UUID orderId, UUID buyerUserId) {
        OrderEntity order = orderRepository.findByIdAndBuyerUserIdForResponse(orderId, buyerUserId).orElse(null);
        return order == null ? null : response(order);
    }

    private SafeOrderResponse response(OrderEntity order) {
        PaymentSessionEntity session = paymentSessionRepository
                .findFirstByOrderIdOrderByCreatedAtAsc(order.getId())
                .orElse(null);
        boolean reviewRequired = session != null && receiptRepository.hasRequiresAction(session.getProviderSessionId());
        return SafeOrderResponse.from(order, reviewRequired);
    }
}
