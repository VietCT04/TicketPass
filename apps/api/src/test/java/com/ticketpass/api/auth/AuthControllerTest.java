package com.ticketpass.api.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketpass.api.user.UserEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = org.mockito.Mockito.mock(AuthService.class);
        SessionCookieFactory cookieFactory = new SessionCookieFactory(false, "");
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, cookieFactory)).build();
    }

    @Test
    void signupReturnsSafeUserResponseAndSessionCookie() throws Exception {
        UserEntity user = user();
        when(authService.signup(any(SignupRequest.class)))
                .thenReturn(new AuthResult(user, "raw-session-token", Duration.ofDays(30)));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "correct horse battery staple",
                                  "display_name": "Avery"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly(SessionCookieFactory.COOKIE_NAME, true))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")))
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.user.display_name").value("Avery"))
                .andExpect(jsonPath("$.user.password_hash").doesNotExist())
                .andExpect(jsonPath("$.sessionToken").doesNotExist());
    }

    @Test
    void loginReturnsSafeUserResponseAndSessionCookie() throws Exception {
        UserEntity user = user();
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResult(user, "raw-session-token", Duration.ofDays(30)));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "correct horse battery staple"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly(SessionCookieFactory.COOKIE_NAME, true))
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.user.password_hash").doesNotExist())
                .andExpect(jsonPath("$.sessionToken").doesNotExist());
    }

    @Test
    void logoutRevokesSessionWhenCookieExistsAndClearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie(SessionCookieFactory.COOKIE_NAME, "raw-session-token")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(SessionCookieFactory.COOKIE_NAME, 0))
                .andExpect(cookie().httpOnly(SessionCookieFactory.COOKIE_NAME, true))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")));

        verify(authService).logout(eq("raw-session-token"));
    }

    @Test
    void logoutWithoutCookieStillClearsCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(SessionCookieFactory.COOKIE_NAME, 0))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Path=/")));

        verify(authService).logout(null);
    }

    private static UserEntity user() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
        ReflectionTestUtils.setField(user, "email", "user@example.com");
        ReflectionTestUtils.setField(user, "displayName", "Avery");
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-07-10T10:00:00Z"));
        return user;
    }
}

