package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import java.time.Clock;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventAutocompleteService {

    private static final int MIN_QUERY_LENGTH = 3;
    private static final int MAX_QUERY_LENGTH = 100;
    private static final int MAX_RESULTS = 10;
    private static final String QUERY_LENGTH_ERROR = "Query must contain between 3 and 100 characters";

    private final EventRepository eventRepository;
    private final Clock clock;

    public EventAutocompleteService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EventAutocompleteResponse autocomplete(String rawQuery) {
        String query = normalizeQuery(rawQuery);
        return EventAutocompleteResponse.from(eventRepository.searchAutocomplete(
                query,
                clock.instant(),
                PageRequest.of(0, MAX_RESULTS)));
    }

    private static String normalizeQuery(String rawQuery) {
        if (rawQuery == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, QUERY_LENGTH_ERROR);
        }

        String query = rawQuery.trim();
        if (query.length() < MIN_QUERY_LENGTH || query.length() > MAX_QUERY_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, QUERY_LENGTH_ERROR);
        }

        return query.toLowerCase(Locale.ROOT);
    }
}
