package com.ticketpass.api.auth;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieFactory {

    public static final String COOKIE_NAME = "ticketpass_session";

    private final boolean secure;

    public SessionCookieFactory(@Value("${ticketpass.auth.cookie-secure:false}") boolean secure) {
        this.secure = secure;
    }

    ResponseCookie create(String sessionToken, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, sessionToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}

