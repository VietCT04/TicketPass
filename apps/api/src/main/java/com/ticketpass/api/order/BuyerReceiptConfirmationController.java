package com.ticketpass.api.order;

import com.ticketpass.api.auth.AuthenticatedUser;
import com.ticketpass.api.common.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class BuyerReceiptConfirmationController {

    private final BuyerReceiptConfirmationService confirmationService;

    public BuyerReceiptConfirmationController(BuyerReceiptConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @PostMapping("/{orderId}/receipt-confirmation")
    public ResponseEntity<BuyerReceiptConfirmationResponse> confirm(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String orderId,
            @RequestBody(required = false) byte[] body) {
        if (body != null && body.length > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Receipt confirmation does not accept a request body");
        }

        BuyerReceiptConfirmationResponse response = confirmationService.confirm(currentUser.id(), orderId);
        HttpStatus status = response.statusRefreshRequired() ? HttpStatus.ACCEPTED : HttpStatus.OK;
        return ResponseEntity.status(status)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(response);
    }
}
