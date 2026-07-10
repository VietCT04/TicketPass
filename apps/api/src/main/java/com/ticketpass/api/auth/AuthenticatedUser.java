package com.ticketpass.api.auth;

import com.ticketpass.api.user.UserEntity;
import java.time.Instant;
import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email,
        String displayName,
        Instant createdAt) {

    static AuthenticatedUser from(UserEntity user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt());
    }
}
