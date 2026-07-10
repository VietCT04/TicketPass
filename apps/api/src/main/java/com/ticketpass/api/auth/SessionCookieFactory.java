package com.ticketpass.api.auth;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieFactory {

    public static final String COOKIE_NAME = "ticketpass_session";
    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE = "Lax";

    private final boolean secure;
    private final String domain;

    public SessionCookieFactory(
            @Value("${ticketpass.auth.cookie-secure:false}") boolean secure,
            @Value("${ticketpass.auth.cookie-domain:}") String domain) {
        this.secure = secure;
        this.domain = domain;
    }

    ResponseCookie create(String sessionToken, Duration maxAge) {
        return baseCookie(sessionToken)
                .maxAge(maxAge)
                .build();
    }

    ResponseCookie clear() {
        return baseCookie("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(SAME_SITE)
                .path(COOKIE_PATH);
        if (domain != null && !domain.isBlank()) {
            builder.domain(domain.trim());
        }
        return builder;
    }
}

