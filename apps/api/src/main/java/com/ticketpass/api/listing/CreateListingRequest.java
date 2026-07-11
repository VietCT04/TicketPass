package com.ticketpass.api.listing;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateListingRequest(
        @JsonProperty("event_name")
        @NotBlank @Size(max = 255) String eventName,
        @JsonProperty("event_venue")
        @NotBlank @Size(max = 255) String eventVenue,
        @JsonProperty("event_city")
        @NotBlank @Size(max = 120) String eventCity,
        @JsonProperty("event_starts_at")
        @NotNull @Future Instant eventStartsAt,
        @JsonProperty("event_platform")
        @NotBlank @Size(max = 120) String eventPlatform,
        @JsonProperty("seat_info")
        @NotBlank @Size(max = 255) String seatInfo,
        @JsonProperty("ticket_type")
        @NotBlank @Size(max = 120) String ticketType,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @JsonProperty("asking_price_minor")
        @Positive long askingPriceMinor,
        @JsonProperty("transfer_method")
        @NotNull TransferMethod transferMethod,
        @JsonProperty("is_transferable_confirmed")
        @AssertTrue boolean transferableConfirmed,
        @JsonProperty("public_notes")
        @Size(max = 1000) String publicNotes) {
}

