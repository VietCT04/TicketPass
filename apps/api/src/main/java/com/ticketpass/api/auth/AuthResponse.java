package com.ticketpass.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticketpass.api.user.UserEntity;

public record AuthResponse(UserResponse user) {

    static AuthResponse from(UserEntity user) {
        return new AuthResponse(UserResponse.from(user));
    }

    static AuthResponse from(AuthenticatedUser user) {
        return new AuthResponse(UserResponse.from(user));
    }

    public record UserResponse(
            String id,
            String email,
            @JsonProperty("display_name") String displayName,
            @JsonProperty("created_at") String createdAt) {

        static UserResponse from(UserEntity user) {
            return new UserResponse(
                    user.getId().toString(),
                    user.getEmail(),
                    user.getDisplayName(),
                    user.getCreatedAt().toString());
        }

        static UserResponse from(AuthenticatedUser user) {
            return new UserResponse(
                    user.id().toString(),
                    user.email(),
                    user.displayName(),
                    user.createdAt().toString());
        }
    }
}

