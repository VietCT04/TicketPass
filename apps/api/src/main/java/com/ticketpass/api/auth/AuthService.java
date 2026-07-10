package com.ticketpass.api.auth;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.user.UserEntity;
import com.ticketpass.api.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    static final Duration SESSION_TTL = Duration.ofDays(30);

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final Clock clock;

    public AuthService(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            SecureRandom secureRandom,
            Clock clock) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = secureRandom;
        this.clock = clock;
    }

    @Transactional
    public AuthResult signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        UserEntity savedUser = userRepository.save(user);

        return createSession(savedUser);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(AuthService::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return createSession(user);
    }

    @Transactional
    public Optional<AuthenticatedUser> authenticateSession(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        Instant now = Instant.now(clock);
        return authSessionRepository.findByTokenHash(hashToken(rawToken))
                .filter(session -> isActive(session, now))
                .map(session -> {
                    session.setLastUsedAt(now);
                    return AuthenticatedUser.from(session.getUser());
                });
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        Instant now = Instant.now(clock);
        authSessionRepository.findByTokenHash(hashToken(rawToken))
                .filter(session -> isActive(session, now))
                .ifPresent(session -> session.setRevokedAt(now));
    }

    private AuthResult createSession(UserEntity user) {
        String rawToken = generateToken();
        Instant now = Instant.now(clock);

        AuthSessionEntity session = new AuthSessionEntity();
        session.setUser(user);
        session.setTokenHash(hashToken(rawToken));
        session.setExpiresAt(now.plus(SESSION_TTL));
        session.setLastUsedAt(now);
        authSessionRepository.save(session);

        return new AuthResult(user, rawToken, SESSION_TTL);
    }

    private static boolean isActive(AuthSessionEntity session, Instant now) {
        return session.getRevokedAt() == null && session.getExpiresAt().isAfter(now);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}

