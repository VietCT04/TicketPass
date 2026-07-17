package com.ticketpass.api.order;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.payment.webhook.CheckoutReconciliationService;
import java.time.Clock;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrderReadService {

    private final OrderRepository orderRepository;
    private final CheckoutReconciliationService reconciliationService;
    private final SafeOrderResponseService safeOrderResponseService;
    private final Clock clock;

    public OrderReadService(
            OrderRepository orderRepository,
            CheckoutReconciliationService reconciliationService,
            SafeOrderResponseService safeOrderResponseService,
            Clock clock) {
        this.orderRepository = orderRepository;
        this.reconciliationService = reconciliationService;
        this.safeOrderResponseService = safeOrderResponseService;
        this.clock = clock;
    }

    public SafeOrderResponse read(UUID buyerUserId, String rawOrderId) {
        UUID orderId = parseOrderId(rawOrderId);
        if (orderRepository.findByIdAndBuyerUserId(orderId, buyerUserId).isEmpty()) {
            throw notFound();
        }
        reconciliationService.reconcileOrderOnRead(orderId, clock.instant());
        SafeOrderResponse response = safeOrderResponseService.forBuyer(orderId, buyerUserId);
        if (response == null) {
            throw notFound();
        }
        return response;
    }

    private static UUID parseOrderId(String rawOrderId) {
        try {
            return UUID.fromString(rawOrderId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId must be a UUID");
        }
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Order not found");
    }
}
