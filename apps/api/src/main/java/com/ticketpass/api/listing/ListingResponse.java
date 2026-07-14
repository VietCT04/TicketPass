package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ListingResponse(
        String id,
        @JsonProperty("seller_id") String sellerId,
        EventResponse event,
        @JsonProperty("event_platform") String eventPlatform,
        @JsonProperty("seat_info") String seatInfo,
        @JsonProperty("ticket_type") String ticketType,
        int quantity,
        String currency,
        @JsonProperty("asking_price_minor") long askingPriceMinor,
        @JsonProperty("transfer_method") TransferMethod transferMethod,
        @JsonProperty("is_transferable_confirmed") boolean transferableConfirmed,
        ListingStatus status,
        @JsonProperty("public_notes") String publicNotes,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt) {

    static ListingResponse from(ListingEntity listing) {
        return new ListingResponse(
                listing.getId().toString(),
                listing.getSeller().getId().toString(),
                EventResponse.from(listing.getEvent()),
                listing.getEventPlatform(),
                listing.getSeatInfo(),
                listing.getTicketType(),
                listing.getQuantity(),
                listing.getCurrency(),
                listing.getAskingPriceMinor(),
                listing.getTransferMethod(),
                listing.isTransferableConfirmed(),
                listing.getStatus(),
                listing.getPublicNotes(),
                listing.getCreatedAt().toString(),
                listing.getUpdatedAt().toString());
    }

    public record EventResponse(
            String id,
            String name,
            String venue,
            String city,
            @JsonProperty("starts_at") String startsAt) {

        static EventResponse from(EventEntity event) {
            return new EventResponse(
                    event.getId().toString(),
                    event.getName(),
                    event.getVenue(),
                    event.getCity(),
                    event.getStartsAt().toString());
        }
    }
}

