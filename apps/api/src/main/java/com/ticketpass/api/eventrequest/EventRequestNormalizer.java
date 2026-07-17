package com.ticketpass.api.eventrequest;

import com.ticketpass.api.common.ApiException;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;

final class EventRequestNormalizer {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    private EventRequestNormalizer() {}

    static NormalizedText requiredText(String field, String value, int maximumLength) {
        if (value == null) throw invalid(field);
        String displayValue = value.strip();
        if (displayValue.isEmpty() || displayValue.length() > maximumLength) throw invalid(field);
        String normalizedValue = WHITESPACE.matcher(displayValue).replaceAll(" ").toLowerCase(Locale.ROOT);
        if (normalizedValue.isEmpty()) throw invalid(field);
        return new NormalizedText(displayValue, normalizedValue);
    }

    private static ApiException invalid(String field) { return new ApiException(HttpStatus.BAD_REQUEST, "Invalid " + field); }
    record NormalizedText(String displayValue, String normalizedValue) {}
}
