package com.ticketpass.api.payment;

import com.ticketpass.api.config.TrustedOriginPolicy;
import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class PaymentConfigurationValidator implements SmartInitializingSingleton {

    private static final String MOCK_WEBHOOK_PATH = "/api/payments/webhooks/mock";
    private final PaymentProperties properties;
    private final TrustedOriginPolicy trustedOriginPolicy;
    private final boolean cookieSecure;

    PaymentConfigurationValidator(
            PaymentProperties properties,
            TrustedOriginPolicy trustedOriginPolicy,
            @Value("${ticketpass.auth.cookie-secure:false}") boolean cookieSecure) {
        this.properties = properties;
        this.trustedOriginPolicy = trustedOriginPolicy;
        this.cookieSecure = cookieSecure;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!"mock".equals(properties.provider())) {
            throw new IllegalStateException("ticketpass.payments.provider must be mock");
        }
        if (properties.mock() == null || !properties.mock().enabled()) {
            throw new IllegalStateException("ticketpass.payments.mock.enabled must be true for the mock provider");
        }
        validateBaseUrl(properties.frontendBaseUrl(), "ticketpass.payments.frontend-base-url");
        validateBaseUrl(properties.mock().providerBaseUrl(), "ticketpass.payments.mock.provider-base-url");
        validateWebhookUrl(properties.mock().webhookUrl());
        validateSecret(properties.mock().webhookSecret());

        if (!properties.mock().allowNonLoopback()
                && (!isLoopback(properties.frontendBaseUrl())
                        || !isLoopback(properties.mock().providerBaseUrl())
                        || !isLoopback(properties.mock().webhookUrl()))) {
            throw new IllegalStateException("mock payment URLs must be loopback unless ticketpass.payments.mock.allow-non-loopback is true");
        }
        if (trustedOriginPolicy.hasNonLoopbackOrHttpsOrigin() && !cookieSecure) {
            throw new IllegalStateException("ticketpass.auth.cookie-secure must be true for HTTPS or non-loopback trusted origins");
        }
    }

    private static void validateBaseUrl(URI uri, String name) {
        validateAbsoluteHttpUrl(uri, name);
        String path = uri.getPath();
        if (path != null && !path.isBlank() && !"/".equals(path)) {
            throw new IllegalStateException(name + " must not contain a path");
        }
    }

    private static void validateWebhookUrl(URI uri) {
        validateAbsoluteHttpUrl(uri, "ticketpass.payments.mock.webhook-url");
        if (!MOCK_WEBHOOK_PATH.equals(uri.getPath())) {
            throw new IllegalStateException("ticketpass.payments.mock.webhook-url must use the mock webhook path");
        }
    }

    private static void validateAbsoluteHttpUrl(URI uri, String name) {
        if (uri == null || uri.isOpaque() || uri.getScheme() == null || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalStateException(name + " must be an absolute HTTP(S) URL without userinfo, query, or fragment");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalStateException(name + " must use HTTP or HTTPS");
        }
        if (!isLoopback(uri) && !"https".equals(scheme)) {
            throw new IllegalStateException(name + " must use HTTPS outside loopback development");
        }
    }

    private static void validateSecret(String secret) {
        if (secret == null || secret.isBlank() || secret.contains("${") || secret.length() < 32) {
            throw new IllegalStateException("MOCK_PAYMENT_WEBHOOK_SECRET must be configured with at least 32 characters");
        }
    }

    private static boolean isLoopback(URI uri) {
        try {
            return InetAddress.getByName(uri.getHost()).isLoopbackAddress();
        } catch (Exception exception) {
            return false;
        }
    }
}
