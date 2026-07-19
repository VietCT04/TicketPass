package com.ticketpass.api.payment.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketpass.api.listing.ListingEntity;
import com.ticketpass.api.listing.ListingRepository;
import com.ticketpass.api.listing.ListingReservationEntity;
import com.ticketpass.api.listing.ListingReservationRepository;
import com.ticketpass.api.listing.ListingReservationStatus;
import com.ticketpass.api.listing.ListingStatus;
import com.ticketpass.api.order.OrderEntity;
import com.ticketpass.api.order.OrderFulfillmentEntity;
import com.ticketpass.api.order.OrderFulfillmentRepository;
import com.ticketpass.api.order.OrderRepository;
import com.ticketpass.api.order.OrderStatus;
import com.ticketpass.api.order.SettlementStatus;
import com.ticketpass.api.order.TransferStatus;
import com.ticketpass.api.payment.PaymentSessionEntity;
import com.ticketpass.api.payment.PaymentSessionRepository;
import com.ticketpass.api.payment.PaymentSessionStatus;
import com.ticketpass.api.payment.PaymentProperties;
import com.ticketpass.api.payment.mock.MockPaymentProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MockWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockWebhookService.class);
    private static final Duration MAX_TIMESTAMP_AGE = Duration.ofMinutes(5);
    private static final int MAX_BODY_BYTES = 16 * 1024;
    private static final int MAX_IDENTIFIER_LENGTH = 120;
    private final ObjectMapper objectMapper;
    private final PaymentWebhookReceiptRepository receiptRepository;
    private final ListingRepository listingRepository;
    private final ListingReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final OrderFulfillmentRepository fulfillmentRepository;
    private final PaymentSessionRepository paymentSessionRepository;
    private final Clock clock;
    private final byte[] webhookSecret;

    MockWebhookService(
            ObjectMapper objectMapper,
            PaymentWebhookReceiptRepository receiptRepository,
            ListingRepository listingRepository,
            ListingReservationRepository reservationRepository,
            OrderRepository orderRepository,
            OrderFulfillmentRepository fulfillmentRepository,
            PaymentSessionRepository paymentSessionRepository,
            Clock clock,
            PaymentProperties paymentProperties) {
        this.objectMapper = objectMapper;
        this.receiptRepository = receiptRepository;
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.paymentSessionRepository = paymentSessionRepository;
        this.clock = clock;
        this.webhookSecret = paymentProperties.mock().webhookSecret().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    void process(byte[] rawBody, String timestamp, String signature) {
        verify(rawBody, timestamp, signature);
        MockWebhookPayload payload = parse(rawBody);
        Instant now = clock.instant();
        UUID receiptId = UUID.randomUUID();
        String storedEventType = supported(payload.eventType()) ? payload.eventType() : "UNSUPPORTED";
        if (!receiptRepository.insert(receiptId, payload, storedEventType, now)) {
            LOGGER.info("Webhook deduplicated provider event {}", payload.eventId());
            return;
        }
        if (!supported(payload.eventType())) {
            receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.IGNORED, now);
            LOGGER.info("Webhook receipt {} ignored", receiptId);
            return;
        }
        if (!"PAYMENT_SUCCEEDED".equals(payload.eventType())) {
            processDeferred(receiptId, payload, now);
            return;
        }
        processSuccess(receiptId, payload, now);
    }

    private void processDeferred(UUID receiptId, MockWebhookPayload payload, Instant now) {
        PaymentSessionEntity session = paymentSessionRepository.findByProviderSessionId(payload.providerSessionId()).orElse(null);
        if (session == null
                || !MockPaymentProvider.PROVIDER.equals(session.getProvider())
                || session.getOrder() == null
                || payload.amountMinor() != session.getOrder().getAmountMinor()
                || !payload.currency().equals(session.getOrder().getCurrency())) {
            requiresAction(receiptId, now);
            return;
        }
        receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.DEFERRED, now);
        LOGGER.info("Webhook receipt {} deferred", receiptId);
    }

    private void processSuccess(UUID receiptId, MockWebhookPayload payload, Instant now) {
        PaymentSessionEntity initialSession = paymentSessionRepository
                .findByProviderSessionId(payload.providerSessionId()).orElse(null);
        if (initialSession == null || !MockPaymentProvider.PROVIDER.equals(initialSession.getProvider())) {
            requiresAction(receiptId, now);
            return;
        }
        OrderEntity initialOrder = initialSession.getOrder();
        ListingReservationEntity initialReservation = initialOrder.getReservation();
        ListingEntity listing = listingRepository.findByIdForPayment(initialReservation.getListing().getId()).orElse(null);
        if (listing == null) {
            requiresAction(receiptId, now);
            return;
        }
        ListingReservationEntity reservation = reservationRepository.findByIdForPayment(initialReservation.getId()).orElse(null);
        OrderEntity order = orderRepository.findByIdForPayment(initialOrder.getId()).orElse(null);
        PaymentSessionEntity session = paymentSessionRepository
                .findByProviderAndProviderSessionIdForPayment(MockPaymentProvider.PROVIDER, payload.providerSessionId())
                .orElse(null);
        if (reservation == null || order == null || session == null) {
            requiresAction(receiptId, now);
            return;
        }
        OrderFulfillmentEntity fulfillment = fulfillmentRepository.findByOrderId(order.getId()).orElse(null);
        if (isSemanticDuplicate(payload, listing, reservation, order, session, fulfillment)) {
            receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.PROCESSED, now);
            return;
        }
        if (!isCompletable(payload, listing, reservation, order, session, fulfillment, now)) {
            requiresAction(receiptId, now);
            return;
        }
        session.setStatus(PaymentSessionStatus.PAID);
        session.setUpdatedAt(now);
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(now);
        order.setUpdatedAt(now);
        listing.setStatus(ListingStatus.SOLD);
        listing.setUpdatedAt(now);
        fulfillmentRepository.save(createFulfillment(order, now));
        receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.PROCESSED, now);
        LOGGER.info("Webhook receipt {} processed", receiptId);
    }

    private static boolean isCompletable(
            MockWebhookPayload payload, ListingEntity listing, ListingReservationEntity reservation,
            OrderEntity order, PaymentSessionEntity session, OrderFulfillmentEntity fulfillment, Instant now) {
        return session.getOrder().getId().equals(order.getId())
                && order.getReservation().getId().equals(reservation.getId())
                && order.getListing().getId().equals(listing.getId())
                && reservation.getListing().getId().equals(listing.getId())
                && payload.amountMinor() == order.getAmountMinor()
                && payload.currency().equals(order.getCurrency())
                && order.getExpiresAt().equals(session.getExpiresAt())
                && order.getExpiresAt().equals(reservation.getExpiresAt())
                && order.getExpiresAt().equals(session.getExpiresAt())
                && order.getExpiresAt().equals(reservation.getExpiresAt())
                && order.getStatus() == OrderStatus.PAYMENT_PENDING
                && session.getStatus() == PaymentSessionStatus.PENDING
                && fulfillment == null
                && reservation.getStatus() == ListingReservationStatus.ACTIVE
                && order.getExpiresAt().isAfter(now)
                && reservation.getExpiresAt().isAfter(now)
                && listing.getStatus() == ListingStatus.RESERVED
                && order.getBuyerUserId().equals(reservation.getBuyerUserId())
                && order.getSellerUserId().equals(listing.getSeller().getId());
    }

    private static boolean isSemanticDuplicate(
            MockWebhookPayload payload, ListingEntity listing, ListingReservationEntity reservation,
            OrderEntity order, PaymentSessionEntity session, OrderFulfillmentEntity fulfillment) {
        return session.getOrder().getId().equals(order.getId())
                && order.getReservation().getId().equals(reservation.getId())
                && order.getListing().getId().equals(listing.getId())
                && reservation.getListing().getId().equals(listing.getId())
                && payload.amountMinor() == order.getAmountMinor()
                && payload.currency().equals(order.getCurrency())
                && session.getStatus() == PaymentSessionStatus.PAID
                && order.getStatus() == OrderStatus.PAID
                && listing.getStatus() == ListingStatus.SOLD
                && fulfillment != null
                && isCoherentFulfillment(order, fulfillment);
    }

    private static OrderFulfillmentEntity createFulfillment(OrderEntity order, Instant now) {
        OrderFulfillmentEntity fulfillment = new OrderFulfillmentEntity();
        fulfillment.setOrder(order);
        fulfillment.setTransferStatus(TransferStatus.AWAITING_SELLER_TRANSFER);
        fulfillment.setSettlementStatus(SettlementStatus.FUNDS_HELD);
        fulfillment.setTransferDeadlineAt(now.plus(Duration.ofMinutes(15)));
        fulfillment.setCreatedAt(now);
        fulfillment.setUpdatedAt(now);
        return fulfillment;
    }

    private static boolean isCoherentFulfillment(OrderEntity order, OrderFulfillmentEntity fulfillment) {
        Instant paidAt = order.getPaidAt();
        if (paidAt == null
                || !paidAt.equals(fulfillment.getCreatedAt())
                || !paidAt.plus(Duration.ofMinutes(15)).equals(fulfillment.getTransferDeadlineAt())
                || fulfillment.getUpdatedAt().isBefore(fulfillment.getCreatedAt())) {
            return false;
        }
        if (fulfillment.getTransferStatus() == TransferStatus.AWAITING_SELLER_TRANSFER) {
            return fulfillment.getSettlementStatus() == SettlementStatus.FUNDS_HELD
                    && fulfillment.getSellerConfirmedAt() == null;
        }
        if (fulfillment.getTransferStatus() == TransferStatus.SELLER_CONFIRMED_TRANSFER) {
            return fulfillment.getSettlementStatus() == SettlementStatus.FUNDS_HELD
                    && fulfillment.getSellerConfirmedAt() != null
                    && fulfillment.getSellerConfirmedAt().isBefore(fulfillment.getTransferDeadlineAt());
        }
        if (fulfillment.getTransferStatus() == TransferStatus.BUYER_CONFIRMED_RECEIPT
                && fulfillment.getBuyerConfirmedAt() == null) {
            return false;
        }
        return fulfillment.getSettlementStatus() != SettlementStatus.RELEASED_TO_SELLER
                || fulfillment.getSettlementReleasedAt() != null;
    }

    private void requiresAction(UUID receiptId, Instant now) {
        receiptRepository.complete(receiptId, PaymentWebhookReceiptStatus.REQUIRES_ACTION, now);
        LOGGER.warn("Webhook receipt {} requires action", receiptId);
    }

    private void verify(byte[] body, String rawTimestamp, String rawSignature) {
        try {
            if (body == null || body.length == 0 || body.length > MAX_BODY_BYTES) {
                throw new IllegalArgumentException();
            }
            if (rawTimestamp == null || !rawTimestamp.matches("[0-9]{1,19}")
                    || rawSignature == null || !rawSignature.matches("v1=[0-9a-f]{64}")) {
                throw new WebhookUnauthorizedException();
            }
            long timestamp = Long.parseLong(rawTimestamp);
            Instant signedAt = Instant.ofEpochSecond(timestamp);
            if (Duration.between(signedAt, clock.instant()).abs().compareTo(MAX_TIMESTAMP_AGE) > 0) {
                throw new WebhookUnauthorizedException();
            }
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret, "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            byte[] expected = mac.doFinal(body);
            byte[] actual = hexToBytes(rawSignature.substring(3));
            if (!MessageDigest.isEqual(expected, actual)) {
                throw new WebhookUnauthorizedException();
            }
        } catch (WebhookUnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new WebhookUnauthorizedException();
        }
    }

    private MockWebhookPayload parse(byte[] rawBody) {
        try {
            JsonNode node = objectMapper.readTree(rawBody);
            String eventId = required(node, "event_id", MAX_IDENTIFIER_LENGTH);
            String eventType = required(node, "event_type", 40);
            String providerSessionId = required(node, "provider_session_id", MAX_IDENTIFIER_LENGTH);
            JsonNode amount = node.get("amount_minor");
            if (amount == null || !amount.canConvertToLong() || amount.longValue() <= 0) throw new IllegalArgumentException();
            String currency = required(node, "currency", 3);
            if (!"VND".equals(currency)) throw new IllegalArgumentException();
            String occurredAt = required(node, "occurred_at", 80);
            return new MockWebhookPayload(eventId, eventType, providerSessionId, amount.longValue(), currency, Instant.parse(occurredAt));
        } catch (Exception exception) {
            throw new IllegalArgumentException();
        }
    }

    private static String required(JsonNode node, String field, int maxLength) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual() || value.textValue().isBlank() || value.textValue().length() > maxLength) {
            throw new IllegalArgumentException();
        }
        return value.textValue();
    }

    private static boolean supported(String eventType) {
        return "PAYMENT_SUCCEEDED".equals(eventType)
                || "PAYMENT_FAILED".equals(eventType)
                || "PAYMENT_CANCELLED".equals(eventType);
    }

    private static byte[] hexToBytes(String value) {
        byte[] bytes = new byte[value.length() / 2];
        for (int index = 0; index < value.length(); index += 2) {
            bytes[index / 2] = (byte) Integer.parseInt(value.substring(index, index + 2), 16);
        }
        return bytes;
    }
}
