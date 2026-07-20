package com.ticketpass.api.listing;

import com.ticketpass.api.common.ApiException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.springframework.http.HttpStatus;

public record EventBrowseFilters(
        String normalizedQuery,
        String queryLikePattern,
        String normalizedCity,
        Instant startsFrom,
        Instant startsBefore) {

    private static final String QUERY_ERROR = "q must contain between 2 and 100 characters";
    private static final String CITY_ERROR = "city must contain between 1 and 120 characters";
    private static final String STARTS_FROM_ERROR = "starts_from must be an RFC 3339 timestamp with an explicit offset";
    private static final String STARTS_BEFORE_ERROR = "starts_before must be an RFC 3339 timestamp with an explicit offset";
    private static final String RANGE_ERROR = "starts_from must be earlier than starts_before";
    private static final char LIKE_ESCAPE = '!';

    public static EventBrowseFilters parse(
            String rawQuery,
            String rawCity,
            String rawStartsFrom,
            String rawStartsBefore) {
        String normalizedQuery = normalizeText(rawQuery);
        String normalizedCity = normalizeText(rawCity);
        validateLength(normalizedQuery, 2, 100, QUERY_ERROR);
        validateLength(normalizedCity, 1, 120, CITY_ERROR);

        Instant startsFrom = parseTimestamp(rawStartsFrom, STARTS_FROM_ERROR);
        Instant startsBefore = parseTimestamp(rawStartsBefore, STARTS_BEFORE_ERROR);
        if (startsFrom != null && startsBefore != null && !startsFrom.isBefore(startsBefore)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, RANGE_ERROR);
        }

        String lowercaseQuery = lowercase(normalizedQuery);
        return new EventBrowseFilters(
                lowercaseQuery,
                lowercaseQuery == null ? null : "%" + escapeLike(lowercaseQuery) + "%",
                lowercase(normalizedCity),
                startsFrom,
                startsBefore);
    }

    private static String normalizeText(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        StringBuilder normalized = new StringBuilder();
        boolean pendingWhitespace = false;
        for (int index = 0; index < rawValue.length();) {
            int codePoint = rawValue.codePointAt(index);
            index += Character.charCount(codePoint);
            if (Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint)) {
                pendingWhitespace = normalized.length() > 0;
            } else {
                if (pendingWhitespace) {
                    normalized.append(' ');
                    pendingWhitespace = false;
                }
                normalized.appendCodePoint(codePoint);
            }
        }
        return normalized.isEmpty() ? null : normalized.toString();
    }

    private static void validateLength(String value, int minimum, int maximum, String message) {
        if (value == null) {
            return;
        }

        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount < minimum || codePointCount > maximum) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static Instant parseTimestamp(String rawValue, String message) {
        if (rawValue == null) {
            return null;
        }

        try {
            return OffsetDateTime.parse(rawValue.strip(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
        } catch (DateTimeParseException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private static String lowercase(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static String escapeLike(String value) {
        String escape = String.valueOf(LIKE_ESCAPE);
        return value
                .replace(escape, escape + escape)
                .replace("%", escape + "%")
                .replace("_", escape + "_");
    }
}
