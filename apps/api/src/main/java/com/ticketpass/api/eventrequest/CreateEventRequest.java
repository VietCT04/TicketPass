package com.ticketpass.api.eventrequest;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateEventRequest(
        @JsonProperty("event_name") String eventName,
        @JsonProperty("starts_at") String startsAt,
        String venue,
        String city,
        @JsonProperty("official_url") String officialUrl) {
}
