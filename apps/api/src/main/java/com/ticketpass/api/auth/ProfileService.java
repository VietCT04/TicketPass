package com.ticketpass.api.auth;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.user.UserEntity;
import com.ticketpass.api.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final Clock clock;

    public ProfileService(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse updateDisplayName(UUID userId, String displayName) {
        String normalizedDisplayName = DisplayNameNormalizer.normalizeForStorage(displayName);
        UserEntity user = userRepository.findByIdForAccountMutation(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        if (normalizedDisplayName.equals(user.getDisplayName())) {
            return AuthResponse.from(user);
        }

        Instant now = Instant.now(clock);
        user.setDisplayName(normalizedDisplayName);
        user.setUpdatedAt(now);
        return AuthResponse.from(user);
    }
}
