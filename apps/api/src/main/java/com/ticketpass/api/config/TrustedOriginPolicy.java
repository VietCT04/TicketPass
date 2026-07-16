package com.ticketpass.api.config;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TrustedOriginPolicy {

    private final Set<String> allowedOrigins;

    public TrustedOriginPolicy(
            @Value("${ticketpass.security.allowed-origins:http://localhost:3000}") List<String> configuredOrigins) {
        if (configuredOrigins == null || configuredOrigins.isEmpty()) {
            throw new IllegalStateException("ticketpass.security.allowed-origins must contain at least one valid origin");
        }

        LinkedHashSet<String> normalizedOrigins = new LinkedHashSet<>();
        for (String configuredOrigin : configuredOrigins) {
            normalizedOrigins.add(normalizeConfiguredOrigin(configuredOrigin));
        }
        allowedOrigins = Set.copyOf(normalizedOrigins);
    }

    public List<String> allowedOrigins() {
        return List.copyOf(allowedOrigins);
    }

    public boolean isAllowedOrigin(String origin) {
        return normalizeRequestOrigin(origin, false)
                .map(allowedOrigins::contains)
                .orElse(false);
    }

    public boolean isAllowedReferer(String referer) {
        return normalizeRequestOrigin(referer, true)
                .map(allowedOrigins::contains)
                .orElse(false);
    }

    private static String normalizeConfiguredOrigin(String configuredOrigin) {
        return normalizeRequestOrigin(configuredOrigin, false)
                .orElseThrow(() -> new IllegalStateException(
                        "ticketpass.security.allowed-origins contains an invalid origin"));
    }

    private static java.util.Optional<String> normalizeRequestOrigin(String value, boolean allowPathAndQuery) {
        if (value == null || value.isBlank()) {
            return java.util.Optional.empty();
        }

        try {
            URI uri = new URI(value.trim());
            if (uri.isOpaque()
                    || uri.getUserInfo() != null
                    || uri.getHost() == null
                    || uri.getScheme() == null
                    || (!allowPathAndQuery
                            && (uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !"/".equals(uri.getRawPath())
                                    || uri.getRawQuery() != null
                                    || uri.getRawFragment() != null))) {
                return java.util.Optional.empty();
            }

            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                return java.util.Optional.empty();
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if (!host.contains(":")) {
                host = IDN.toASCII(host);
            }
            int port = effectivePort(scheme, uri.getPort());
            if (port < 0) {
                return java.util.Optional.empty();
            }

            String normalizedHost = host.contains(":") ? "[" + host + "]" : host;
            boolean defaultPort = (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
            return java.util.Optional.of(scheme + "://" + normalizedHost + (defaultPort ? "" : ":" + port));
        } catch (URISyntaxException | IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private static int effectivePort(String scheme, int explicitPort) {
        if (explicitPort >= 0) {
            return explicitPort;
        }
        if (scheme.equals("http")) {
            return 80;
        }
        if (scheme.equals("https")) {
            return 443;
        }
        return -1;
    }
}
