package com.ticketpass.api.eventrequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record EventRequestResponse(
        UUID id,
        EventRequestStatus status,
        @JsonProperty("event_name") String eventName,
        @JsonProperty("starts_at") Instant startsAt,
        String venue,
        String city,
        @JsonProperty("official_url") String officialUrl,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt) {

    static EventRequestResponse from(EventRequestEntity request) {
        return new EventRequestResponse(
                request.getId(), request.getStatus(), request.getEventName(), request.getStartsAt(),
                request.getVenue(), request.getCity(), request.getOfficialUrl(),
                request.getCreatedAt(), request.getUpdatedAt());
    }
}
