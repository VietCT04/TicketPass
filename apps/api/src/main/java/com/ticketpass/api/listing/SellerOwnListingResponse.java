package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SellerOwnListingResponse(
        String id,
        ListingStatus status,
        @JsonProperty("event_platform") String eventPlatform,
        @JsonProperty("seat_info") String seatInfo,
        @JsonProperty("ticket_type") String ticketType,
        int quantity,
        @JsonProperty("asking_price_minor") long askingPriceMinor,
        String currency,
        @JsonProperty("transfer_method") TransferMethod transferMethod,
        @JsonProperty("is_transferable_confirmed") boolean transferableConfirmed,
        @JsonProperty("public_notes") String publicNotes,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt,
        Event event) {

    static SellerOwnListingResponse from(SellerOwnListingRow row) {
        return new SellerOwnListingResponse(
                row.id().toString(),
                row.status(),
                row.eventPlatform(),
                row.seatInfo(),
                row.ticketType(),
                row.quantity(),
                row.askingPriceMinor(),
                row.currency(),
                row.transferMethod(),
                row.transferableConfirmed(),
                row.publicNotes(),
                row.createdAt().toString(),
                row.updatedAt().toString(),
                new Event(
                        row.eventId().toString(),
                        row.eventName(),
                        row.eventStartsAt().toString(),
                        row.eventVenue(),
                        row.eventCity()));
    }

    public record Event(
            String id,
            String name,
            @JsonProperty("starts_at") String startsAt,
            String venue,
            String city) {
    }
}
