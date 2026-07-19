package com.ticketpass.api.order;

import com.ticketpass.api.auth.AuthenticatedUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerTransferConfirmationController {

    private final SellerTransferConfirmationService confirmationService;

    public SellerTransferConfirmationController(SellerTransferConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @PostMapping("/{orderId}/transfer-confirmation")
    public ResponseEntity<SellerTransferConfirmationResponse> confirmTransfer(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String orderId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(confirmationService.confirm(currentUser.id(), orderId));
    }
}
