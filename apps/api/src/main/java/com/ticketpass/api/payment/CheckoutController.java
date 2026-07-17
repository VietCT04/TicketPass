package com.ticketpass.api.payment;

import com.ticketpass.api.auth.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/{reservationId}/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String reservationId) {
        CheckoutService.CheckoutResult result = checkoutService.checkout(currentUser.id(), reservationId);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(result.response());
    }
}
