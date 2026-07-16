package com.ticketpass.api.auth;

import com.ticketpass.api.config.TrustedOriginPolicy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CsrfOriginValidationFilter extends OncePerRequestFilter {

    private static final Set<String> UNSAFE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final TrustedOriginPolicy trustedOriginPolicy;

    public CsrfOriginValidationFilter(TrustedOriginPolicy trustedOriginPolicy) {
        this.trustedOriginPolicy = trustedOriginPolicy;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (requiresOriginValidation(request) && !hasAllowedOrigin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Invalid request origin\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresOriginValidation(HttpServletRequest request) {
        return isApiRequest(request)
                && UNSAFE_METHODS.contains(request.getMethod())
                && hasSessionCookie(request);
    }

    private static boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/api") || path.startsWith("/api/");
    }

    private static boolean hasSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(cookie -> SessionCookieFactory.COOKIE_NAME.equals(cookie.getName()));
    }

    private boolean hasAllowedOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            return trustedOriginPolicy.isAllowedOrigin(origin);
        }
        return trustedOriginPolicy.isAllowedReferer(request.getHeader("Referer"));
    }
}
