package com.ticketpass.api.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionCookieFactory sessionCookieFactory;

    public AuthController(AuthService authService, SessionCookieFactory sessionCookieFactory) {
        this.authService = authService;
        this.sessionCookieFactory = sessionCookieFactory;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest request, HttpServletResponse response) {
        AuthResult result = authService.signup(request);
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookieFactory.create(result.sessionToken(), result.maxAge()).toString());
        return AuthResponse.from(result.user());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResult result = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookieFactory.create(result.sessionToken(), result.maxAge()).toString());
        return AuthResponse.from(result.user());
    }
}

