package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record EventAutocompleteResponse(List<EventSummary> events) {

    static EventAutocompleteResponse from(List<EventEntity> events) {
        return new EventAutocompleteResponse(events.stream()
                .map(EventSummary::from)
                .toList());
    }

    public record EventSummary(
            String id,
            String name,
            @JsonProperty("starts_at") String startsAt,
            String venue,
            String city) {

        static EventSummary from(EventEntity event) {
            return new EventSummary(
                    event.getId().toString(),
                    event.getName(),
                    event.getStartsAt().toString(),
                    event.getVenue(),
                    event.getCity());
        }
    }
}
