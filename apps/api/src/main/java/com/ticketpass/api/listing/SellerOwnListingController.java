package com.ticketpass.api.listing;

import com.ticketpass.api.auth.AuthenticatedUser;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SellerOwnListingController {

    private final SellerOwnListingService sellerOwnListingService;

    public SellerOwnListingController(SellerOwnListingService sellerOwnListingService) {
        this.sellerOwnListingService = sellerOwnListingService;
    }

    @GetMapping("/api/me/listings")
    public ResponseEntity<SellerOwnListingPageResponse> getSellerListings(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(name = "page", required = false) String page,
            @RequestParam(name = "page_size", required = false) String pageSize,
            @RequestParam(name = "status", required = false) String status) {
        SellerOwnListingPageResponse response = sellerOwnListingService.getSellerListings(
                currentUser.id(),
                page,
                pageSize,
                status);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}
