package com.ticketpass.api.eventrequest;

import com.ticketpass.api.common.ApiException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EventRequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventRequestService.class);
    private static final int EVENT_NAME_MAX_LENGTH = 255;
    private static final int VENUE_MAX_LENGTH = 255;
    private static final int CITY_MAX_LENGTH = 120;
    private static final int OFFICIAL_URL_MAX_LENGTH = 2048;
    private final EventRequestRepository eventRequestRepository;
    private final EventRequestPersistenceService persistenceService;
    private final Clock clock;

    public EventRequestService(EventRequestRepository eventRequestRepository, EventRequestPersistenceService persistenceService, Clock clock) {
        this.eventRequestRepository = eventRequestRepository;
        this.persistenceService = persistenceService;
        this.clock = clock;
    }

    public EventRequestCreateResult createOrReturn(UUID requesterUserId, CreateEventRequest request) {
        Instant now = clock.instant();
        ValidatedRequest validated = validate(request, now);
        EventRequestEntity duplicate = findDuplicate(requesterUserId, validated);
        if (duplicate != null) return result(duplicate, false);
        try {
            return result(persistenceService.saveAndFlush(newRequest(requesterUserId, validated, now)), true);
        } catch (DataIntegrityViolationException exception) {
            EventRequestEntity recovered = findDuplicate(requesterUserId, validated);
            if (recovered != null) return result(recovered, false);
            throw exception;
        }
    }

    private EventRequestEntity findDuplicate(UUID requesterUserId, ValidatedRequest request) {
        return eventRequestRepository.findPendingDuplicate(requesterUserId, request.eventName().normalizedValue(),
                request.startsAt(), request.venue().normalizedValue(), request.city().normalizedValue()).orElse(null);
    }

    private static EventRequestCreateResult result(EventRequestEntity request, boolean created) {
        LOGGER.info("Event request {} {}", request.getId(), created ? "created" : "recovered");
        return new EventRequestCreateResult(request, created);
    }

    private static EventRequestEntity newRequest(UUID requesterUserId, ValidatedRequest request, Instant now) {
        EventRequestEntity entity = new EventRequestEntity();
        entity.setId(UUID.randomUUID());
        entity.setRequesterUserId(requesterUserId);
        entity.setEventName(request.eventName().displayValue());
        entity.setNormalizedEventName(request.eventName().normalizedValue());
        entity.setStartsAt(request.startsAt());
        entity.setVenue(request.venue().displayValue());
        entity.setNormalizedVenue(request.venue().normalizedValue());
        entity.setCity(request.city().displayValue());
        entity.setNormalizedCity(request.city().normalizedValue());
        entity.setOfficialUrl(request.officialUrl());
        entity.setStatus(EventRequestStatus.PENDING);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private static ValidatedRequest validate(CreateEventRequest request, Instant now) {
        if (request == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Request validation failed");
        EventRequestNormalizer.NormalizedText eventName = EventRequestNormalizer.requiredText("event_name", request.eventName(), EVENT_NAME_MAX_LENGTH);
        EventRequestNormalizer.NormalizedText venue = EventRequestNormalizer.requiredText("venue", request.venue(), VENUE_MAX_LENGTH);
        EventRequestNormalizer.NormalizedText city = EventRequestNormalizer.requiredText("city", request.city(), CITY_MAX_LENGTH);
        return new ValidatedRequest(eventName, parseFutureStartsAt(request.startsAt(), now), venue, city, validateOfficialUrl(request.officialUrl()));
    }

    private static Instant parseFutureStartsAt(String value, Instant now) {
        if (value == null) throw invalid("starts_at");
        try {
            Instant startsAt = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
            if (!startsAt.isAfter(now)) throw invalid("starts_at");
            return startsAt;
        } catch (DateTimeParseException exception) { throw invalid("starts_at"); }
    }

    private static String validateOfficialUrl(String value) {
        if (value == null) return null;
        String url = value.strip();
        if (url.isEmpty() || url.length() > OFFICIAL_URL_MAX_LENGTH) throw invalid("official_url");
        try {
            URI uri = new URI(url);
            if (!uri.isAbsolute() || !"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getHost().isBlank() || uri.getUserInfo() != null) throw invalid("official_url");
            return url;
        } catch (URISyntaxException exception) { throw invalid("official_url"); }
    }

    private static ApiException invalid(String field) { return new ApiException(HttpStatus.BAD_REQUEST, "Invalid " + field); }

    private record ValidatedRequest(EventRequestNormalizer.NormalizedText eventName, Instant startsAt,
            EventRequestNormalizer.NormalizedText venue, EventRequestNormalizer.NormalizedText city, String officialUrl) {}
}
