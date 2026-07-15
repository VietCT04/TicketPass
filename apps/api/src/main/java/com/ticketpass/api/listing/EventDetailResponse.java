package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record EventDetailResponse(
        EventSummary event,
        List<ListingSummary> listings,
        Pagination pagination) {

    static EventDetailResponse from(EventEntity event, Page<EventListingSummaryRow> listings, PublicPagination pagination) {
        return new EventDetailResponse(
                EventSummary.from(event),
                listings.getContent().stream().map(ListingSummary::from).toList(),
                new Pagination(
                        pagination.page(),
                        pagination.pageSize(),
                        listings.getTotalElements(),
                        listings.getTotalPages()));
    }

    public record EventSummary(
            String id,
            String name,
            @JsonProperty("starts_at") String startsAt,
            String venue,
            String city,
            @JsonProperty("image_url") String imageUrl) {

        static EventSummary from(EventEntity event) {
            return new EventSummary(
                    event.getId().toString(),
                    event.getName(),
                    event.getStartsAt().toString(),
                    event.getVenue(),
                    event.getCity(),
                    null);
        }
    }

    public record ListingSummary(
            String id,
            @JsonProperty("ticket_type") String ticketType,
            @JsonProperty("seat_info") String seatInfo,
            @JsonProperty("event_platform") String eventPlatform,
            @JsonProperty("asking_price_minor") long askingPriceMinor,
            String currency,
            @JsonProperty("transfer_method") TransferMethod transferMethod) {

        static ListingSummary from(EventListingSummaryRow row) {
            return new ListingSummary(
                    row.id().toString(),
                    row.ticketType(),
                    row.seatInfo(),
                    row.eventPlatform(),
                    row.askingPriceMinor(),
                    row.currency(),
                    row.transferMethod());
        }
    }

    public record Pagination(
            int page,
            @JsonProperty("page_size") int pageSize,
            @JsonProperty("total_items") long totalItems,
            @JsonProperty("total_pages") int totalPages) {
    }
}
