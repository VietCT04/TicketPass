package com.ticketpass.api.order;

import com.ticketpass.api.auth.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderReadService orderReadService;

    public OrderController(OrderReadService orderReadService) {
        this.orderReadService = orderReadService;
    }

    @GetMapping("/{orderId}")
    public SafeOrderResponse getOrder(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String orderId) {
        return orderReadService.read(currentUser.id(), orderId);
    }
}
