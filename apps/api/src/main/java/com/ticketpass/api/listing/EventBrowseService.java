package com.ticketpass.api.listing;

import java.time.Clock;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventBrowseService {

    private final EventRepository eventRepository;
    private final Clock clock;

    public EventBrowseService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EventBrowseResponse browse(
            String rawPage,
            String rawPageSize,
            String rawQuery,
            String rawCity,
            String rawStartsFrom,
            String rawStartsBefore) {
        PublicPagination pagination = PublicPagination.parse(rawPage, rawPageSize);
        EventBrowseFilters filters = EventBrowseFilters.parse(rawQuery, rawCity, rawStartsFrom, rawStartsBefore);
        Instant now = clock.instant();
        Page<EventBrowseRow> events = eventRepository.browsePublicEvents(
                ListingStatus.ACTIVE,
                ListingEntity.MVP_CURRENCY,
                now,
                filters.queryLikePattern(),
                filters.normalizedCity(),
                filters.startsFrom(),
                filters.startsBefore(),
                pagination.toPageRequest());
        return EventBrowseResponse.from(events, pagination.page(), pagination.pageSize());
    }
}
