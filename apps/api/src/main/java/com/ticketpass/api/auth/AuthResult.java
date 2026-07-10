package com.ticketpass.api.auth;

import com.ticketpass.api.user.UserEntity;
import java.time.Duration;

record AuthResult(UserEntity user, String sessionToken, Duration maxAge) {
}

