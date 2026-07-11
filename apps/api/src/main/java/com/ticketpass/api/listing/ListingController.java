package com.ticketpass.api.listing;

import com.ticketpass.api.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ListingResponse createListing(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateListingRequest request) {
        return ListingResponse.from(listingService.createListing(currentUser.id(), request));
    }
}

