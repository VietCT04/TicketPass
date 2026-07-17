package com.ticketpass.api.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(TrustedOriginPolicy trustedOriginPolicy) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(trustedOriginPolicy.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Accept", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return request -> isWebhook(request) ? null : source.getCorsConfiguration(request);
    }

    private static boolean isWebhook(HttpServletRequest request) {
        return "/api/payments/webhooks/mock".equals(request.getRequestURI().substring(request.getContextPath().length()));
    }
}
