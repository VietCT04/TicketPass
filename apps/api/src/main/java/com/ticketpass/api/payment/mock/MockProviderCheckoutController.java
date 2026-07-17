package com.ticketpass.api.payment.mock;

import com.ticketpass.api.payment.PaymentSessionResult;
import com.ticketpass.api.payment.PaymentSessionStatus;
import com.ticketpass.api.payment.PaymentProperties;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/mock-provider")
public class MockProviderCheckoutController {

    private final MockPaymentProvider mockPaymentProvider;
    private final URI frontendBaseUrl;

    public MockProviderCheckoutController(
            MockPaymentProvider mockPaymentProvider,
            PaymentProperties paymentProperties) {
        this.mockPaymentProvider = mockPaymentProvider;
        this.frontendBaseUrl = paymentProperties.frontendBaseUrl();
    }

    @GetMapping(value = "/checkout/{providerSessionId}", produces = "text/html")
    @ResponseBody
    public ResponseEntity<String> checkoutPage(@PathVariable String providerSessionId) {
        String canonicalId = canonicalId(providerSessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Content-Security-Policy", "default-src 'none'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'")
                .header("X-Content-Type-Options", "nosniff")
                .header("Referrer-Policy", "no-referrer")
                .body(page(canonicalId, mockPaymentProvider.getSession(canonicalId)));
    }

    @PostMapping("/sessions/{providerSessionId}/succeed")
    public ResponseEntity<Void> succeed(@PathVariable String providerSessionId) {
        return complete(providerSessionId, MockProviderSessionStatus.PAID, "success");
    }

    @PostMapping("/sessions/{providerSessionId}/fail")
    public ResponseEntity<Void> fail(@PathVariable String providerSessionId) {
        return complete(providerSessionId, MockProviderSessionStatus.FAILED, "failed");
    }

    @PostMapping("/sessions/{providerSessionId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String providerSessionId) {
        return complete(providerSessionId, MockProviderSessionStatus.CANCELLED, "cancelled");
    }

    @ExceptionHandler(MockProviderSessionNotFoundException.class)
    ResponseEntity<Void> handleMissingSession() {
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Void> complete(
            String providerSessionId,
            MockProviderSessionStatus targetStatus,
            String returnStatus) {
        String canonicalId = canonicalId(providerSessionId);
        PaymentSessionResult result = mockPaymentProvider.transition(canonicalId, targetStatus);
        if (result.status() != PaymentSessionStatus.valueOf(targetStatus.name())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .location(UriComponentsBuilder.fromUri(frontendBaseUrl)
                        .pathSegment("checkout", mockPaymentProvider.orderIdForRedirect(canonicalId).toString())
                        .queryParam("provider_return", returnStatus)
                        .build().toUri())
                .build();
    }

    private static String page(String providerSessionId, PaymentSessionResult session) {
        String escapedId = HtmlUtils.htmlEscape(providerSessionId);
        String controls = session.status() == PaymentSessionStatus.PENDING
                ? "<form method=\"post\" action=\"/mock-provider/sessions/" + escapedId + "/succeed\">"
                        + "<button>Pay successfully</button></form>"
                        + "<form method=\"post\" action=\"/mock-provider/sessions/" + escapedId + "/fail\">"
                        + "<button>Decline payment</button></form>"
                        + "<form method=\"post\" action=\"/mock-provider/sessions/" + escapedId + "/cancel\">"
                        + "<button>Cancel</button></form>"
                : "";
        return "<!doctype html><html><body><main>"
                + "<p>Amount: " + HtmlUtils.htmlEscape(Long.toString(session.amountMinor())) + "</p>"
                + "<p>Currency: " + HtmlUtils.htmlEscape(session.currency()) + "</p>"
                + "<p>Expires at: " + HtmlUtils.htmlEscape(session.expiresAt().toString()) + "</p>"
                + "<p>Provider session state: " + HtmlUtils.htmlEscape(session.status().name()) + "</p>"
                + controls + "</main></body></html>";
    }

    private static String canonicalId(String providerSessionId) {
        try {
            UUID id = UUID.fromString(providerSessionId);
            if (!id.toString().equals(providerSessionId)) {
                throw new IllegalArgumentException();
            }
            return id.toString();
        } catch (IllegalArgumentException exception) {
            throw new MockProviderSessionNotFoundException();
        }
    }
}
