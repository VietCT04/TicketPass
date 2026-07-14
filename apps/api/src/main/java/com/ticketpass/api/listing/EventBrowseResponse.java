package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record EventBrowseResponse(
        List<EventSummary> events,
        Pagination pagination) {

    static EventBrowseResponse from(Page<EventBrowseRow> page, int requestedPage, int pageSize) {
        return new EventBrowseResponse(
                page.getContent().stream().map(EventSummary::from).toList(),
                new Pagination(requestedPage, pageSize, page.getTotalElements(), page.getTotalPages()));
    }

    public record EventSummary(
            String id,
            String name,
            @JsonProperty("starts_at") String startsAt,
            String venue,
            String city,
            @JsonProperty("image_url") String imageUrl,
            @JsonProperty("lowest_price_minor") long lowestPriceMinor,
            String currency,
            @JsonProperty("available_listing_count") long availableListingCount) {

        static EventSummary from(EventBrowseRow row) {
            return new EventSummary(
                    row.id().toString(),
                    row.name(),
                    row.startsAt().toString(),
                    row.venue(),
                    row.city(),
                    null,
                    row.lowestPriceMinor(),
                    ListingEntity.MVP_CURRENCY,
                    row.availableListingCount());
        }
    }

    public record Pagination(
            int page,
            @JsonProperty("page_size") int pageSize,
            @JsonProperty("total_items") long totalItems,
            @JsonProperty("total_pages") int totalPages) {
    }
}
