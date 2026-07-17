package com.ticketpass.api.payment.webhook;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/webhooks")
public class MockWebhookController {

    private final MockWebhookService webhookService;

    MockWebhookController(MockWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/mock")
    public ResponseEntity<Void> receive(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Mock-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Mock-Signature", required = false) String signature) {
        webhookService.process(rawBody, timestamp, signature);
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(WebhookUnauthorizedException.class)
    ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest() {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid webhook payload"));
    }
}
