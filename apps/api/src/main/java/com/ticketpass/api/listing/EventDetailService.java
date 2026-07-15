package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventDetailService {

    private final EventRepository eventRepository;
    private final ListingRepository listingRepository;
    private final Clock clock;

    public EventDetailService(EventRepository eventRepository, ListingRepository listingRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.listingRepository = listingRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(String rawEventId, String rawPage, String rawPageSize) {
        UUID eventId = parseEventId(rawEventId);
        PublicPagination pagination = PublicPagination.parse(rawPage, rawPageSize);
        Instant now = clock.instant();

        EventEntity event = eventRepository.findPublicUpcomingEventById(eventId, now)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        Page<EventListingSummaryRow> listings = listingRepository.findPublicEventListings(
                eventId,
                ListingStatus.ACTIVE,
                ListingEntity.MVP_CURRENCY,
                now,
                pagination.toPageRequest());
        return EventDetailResponse.from(event, listings, pagination);
    }

    private static UUID parseEventId(String rawEventId) {
        try {
            return UUID.fromString(rawEventId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "eventId must be a valid UUID");
        }
    }
}
