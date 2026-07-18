package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record SellerOwnListingPageResponse(
        List<SellerOwnListingResponse> items,
        int page,
        @JsonProperty("page_size") int pageSize,
        @JsonProperty("total_items") long totalItems,
        @JsonProperty("total_pages") int totalPages) {

    static SellerOwnListingPageResponse from(
            Page<SellerOwnListingRow> listings,
            SellerOwnListingQueryParser query) {
        return new SellerOwnListingPageResponse(
                listings.getContent().stream().map(SellerOwnListingResponse::from).toList(),
                query.page(),
                query.pageSize(),
                listings.getTotalElements(),
                listings.getTotalPages());
    }
}
