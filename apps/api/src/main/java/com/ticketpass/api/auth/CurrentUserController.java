package com.ticketpass.api.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserController {

    @GetMapping("/api/me")
    public AuthResponse currentUser(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return AuthResponse.from(currentUser);
    }
}
