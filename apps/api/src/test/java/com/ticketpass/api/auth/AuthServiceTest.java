package com.ticketpass.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.user.UserEntity;
import com.ticketpass.api.user.UserRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthSessionRepository authSessionRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        authSessionRepository = org.mockito.Mockito.mock(AuthSessionRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                userRepository,
                authSessionRepository,
                passwordEncoder,
                new SecureRandom(),
                Clock.fixed(Instant.parse("2026-07-10T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void signupCreatesUserWithNormalizedEmailHashedPasswordAndSession() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authSessionRepository.save(any(AuthSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResult result = authService.signup(new SignupRequest(
                "  User@Example.COM ",
                "correct horse battery staple",
                "  Avery  "));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getDisplayName()).isEqualTo("Avery");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("correct horse battery staple");
        assertThat(passwordEncoder.matches("correct horse battery staple", savedUser.getPasswordHash())).isTrue();
        assertThat(result.sessionToken()).isNotBlank();

        ArgumentCaptor<AuthSessionEntity> sessionCaptor = ArgumentCaptor.forClass(AuthSessionEntity.class);
        verify(authSessionRepository).save(sessionCaptor.capture());
        AuthSessionEntity session = sessionCaptor.getValue();
        assertThat(session.getUser()).isSameAs(savedUser);
        assertThat(session.getTokenHash()).isNotBlank();
        assertThat(session.getTokenHash()).isNotEqualTo(result.sessionToken());
        assertThat(session.getExpiresAt()).isEqualTo(Instant.parse("2026-08-09T10:00:00Z"));
    }

    @Test
    void signupRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(new SignupRequest(
                        "user@example.com",
                        "correct horse battery staple",
                        "Avery")))
                .isInstanceOf(ApiException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void loginCreatesSessionForValidCredentials() {
        UserEntity user = userWithPassword("user@example.com", passwordEncoder.encode("correct horse battery staple"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(authSessionRepository.save(any(AuthSessionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResult result = authService.login(new LoginRequest("USER@example.com", "correct horse battery staple"));

        assertThat(result.user()).isSameAs(user);
        assertThat(result.sessionToken()).isNotBlank();
        verify(authSessionRepository).save(any(AuthSessionEntity.class));
    }

    @Test
    void loginRejectsInvalidCredentialsWithGenericUnauthorizedError() {
        UserEntity user = userWithPassword("user@example.com", passwordEncoder.encode("correct horse battery staple"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrong password")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Invalid email or password")
                .extracting("status")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void authenticateSessionReturnsMinimalPrincipalAndUpdatesLastUsedForActiveSession() {
        UserEntity user = userWithPassword("user@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-07-01T10:00:00Z"));
        AuthSessionEntity session = activeSession(user);
        when(authSessionRepository.findByTokenHash(AuthService.hashToken("raw-session-token")))
                .thenReturn(Optional.of(session));

        Optional<AuthenticatedUser> result = authService.authenticateSession("raw-session-token");

        assertThat(result).hasValueSatisfying(principal -> {
            assertThat(principal.id()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            assertThat(principal.email()).isEqualTo("user@example.com");
            assertThat(principal.displayName()).isEqualTo("Avery");
            assertThat(principal.createdAt()).isEqualTo(Instant.parse("2026-07-01T10:00:00Z"));
        });
        assertThat(session.getLastUsedAt()).isEqualTo(Instant.parse("2026-07-10T10:00:00Z"));
    }

    @Test
    void authenticateSessionRejectsExpiredSession() {
        UserEntity user = userWithPassword("user@example.com", "hash");
        AuthSessionEntity session = activeSession(user);
        session.setExpiresAt(Instant.parse("2026-07-10T09:59:59Z"));
        when(authSessionRepository.findByTokenHash(AuthService.hashToken("raw-session-token")))
                .thenReturn(Optional.of(session));

        assertThat(authService.authenticateSession("raw-session-token")).isEmpty();
    }

    @Test
    void authenticateSessionRejectsRevokedSession() {
        UserEntity user = userWithPassword("user@example.com", "hash");
        AuthSessionEntity session = activeSession(user);
        session.setRevokedAt(Instant.parse("2026-07-10T09:00:00Z"));
        when(authSessionRepository.findByTokenHash(AuthService.hashToken("raw-session-token")))
                .thenReturn(Optional.of(session));

        assertThat(authService.authenticateSession("raw-session-token")).isEmpty();
    }

    @Test
    void authenticateSessionRejectsUnknownSession() {
        when(authSessionRepository.findByTokenHash(AuthService.hashToken("raw-session-token")))
                .thenReturn(Optional.empty());

        assertThat(authService.authenticateSession("raw-session-token")).isEmpty();
    }

    @Test
    void logoutRevokesActiveSession() {
        UserEntity user = userWithPassword("user@example.com", "hash");
        AuthSessionEntity session = activeSession(user);
        when(authSessionRepository.findByTokenHash(AuthService.hashToken("raw-session-token")))
                .thenReturn(Optional.of(session));

        authService.logout("raw-session-token");

        assertThat(session.getRevokedAt()).isEqualTo(Instant.parse("2026-07-10T10:00:00Z"));
    }

    @Test
    void logoutDoesNothingForMissingToken() {
        authService.logout(null);

        verify(authSessionRepository, never()).findByTokenHash(any());
    }

    private static UserEntity userWithPassword(String email, String passwordHash) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setDisplayName("Avery");
        return user;
    }

    private static AuthSessionEntity activeSession(UserEntity user) {
        AuthSessionEntity session = new AuthSessionEntity();
        session.setUser(user);
        session.setTokenHash(AuthService.hashToken("raw-session-token"));
        session.setExpiresAt(Instant.parse("2026-08-09T10:00:00Z"));
        session.setLastUsedAt(Instant.parse("2026-07-10T09:00:00Z"));
        return session;
    }
}

