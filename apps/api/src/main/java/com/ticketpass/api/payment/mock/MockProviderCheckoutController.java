package com.ticketpass.api.payment.mock;

import com.ticketpass.api.payment.PaymentSessionResult;
import com.ticketpass.api.payment.PaymentSessionStatus;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/mock-provider")
public class MockProviderCheckoutController {

    private final MockPaymentProvider mockPaymentProvider;
    private final String frontendBaseUrl;

    public MockProviderCheckoutController(
            MockPaymentProvider mockPaymentProvider,
            @Value("${ticketpass.payments.frontend-base-url:http://localhost:3000}") String frontendBaseUrl) {
        this.mockPaymentProvider = mockPaymentProvider;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
    }

    @GetMapping(value = "/checkout/{providerSessionId}", produces = "text/html")
    @ResponseBody
    public String checkoutPage(@PathVariable String providerSessionId) {
        return page(providerSessionId, mockPaymentProvider.getSession(providerSessionId));
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
        PaymentSessionResult result = mockPaymentProvider.transition(providerSessionId, targetStatus);
        if (result.status() != PaymentSessionStatus.valueOf(targetStatus.name())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create(frontendBaseUrl + "/checkout/" + mockPaymentProvider.orderIdForRedirect(providerSessionId)
                        + "?provider_return=" + returnStatus))
                .build();
    }

    private static String page(String providerSessionId, PaymentSessionResult session) {
        String controls = session.status() == PaymentSessionStatus.PENDING
                ? "<form method=\"post\" action=\"/mock-provider/sessions/" + providerSessionId + "/succeed\">"
                        + "<button>Pay successfully</button></form>"
                        + "<form method=\"post\" action=\"/mock-provider/sessions/" + providerSessionId + "/fail\">"
                        + "<button>Decline payment</button></form>"
                        + "<form method=\"post\" action=\"/mock-provider/sessions/" + providerSessionId + "/cancel\">"
                        + "<button>Cancel</button></form>"
                : "";
        return "<!doctype html><html><body><main>"
                + "<p>Amount: " + session.amountMinor() + "</p>"
                + "<p>Currency: " + session.currency() + "</p>"
                + "<p>Expires at: " + session.expiresAt() + "</p>"
                + "<p>Provider session state: " + session.status() + "</p>"
                + controls + "</main></body></html>";
    }
}
