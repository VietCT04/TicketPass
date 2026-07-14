package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import java.time.Clock;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventBrowseService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final String PAGE_ERROR = "page must be an integer greater than or equal to 1";
    private static final String PAGE_SIZE_ERROR = "page_size must be an integer between 1 and 50";

    private final EventRepository eventRepository;
    private final Clock clock;

    public EventBrowseService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EventBrowseResponse browse(String rawPage, String rawPageSize) {
        int page = parsePage(rawPage);
        int pageSize = parsePageSize(rawPageSize);
        Instant now = clock.instant();
        Page<EventBrowseRow> events = eventRepository.browsePublicEvents(
                ListingStatus.ACTIVE,
                ListingEntity.MVP_CURRENCY,
                now,
                PageRequest.of(page - 1, pageSize));
        return EventBrowseResponse.from(events, page, pageSize);
    }

    private static int parsePage(String rawPage) {
        if (rawPage == null) {
            return DEFAULT_PAGE;
        }

        int page = parsePositiveInteger(rawPage, PAGE_ERROR);
        if (page < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, PAGE_ERROR);
        }
        return page;
    }

    private static int parsePageSize(String rawPageSize) {
        if (rawPageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }

        int pageSize = parsePositiveInteger(rawPageSize, PAGE_SIZE_ERROR);
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, PAGE_SIZE_ERROR);
        }
        return pageSize;
    }

    private static int parsePositiveInteger(String rawValue, String message) {
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
