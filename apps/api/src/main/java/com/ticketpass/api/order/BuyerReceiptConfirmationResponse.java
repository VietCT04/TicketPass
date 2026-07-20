package com.ticketpass.api.order;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
public record BuyerReceiptConfirmationResponse(
        @JsonProperty("order_id") String orderId, @JsonProperty("payment_status") String paymentStatus,
        @JsonProperty("transfer_status") String transferStatus, @JsonProperty("settlement_status") String settlementStatus,
        @JsonProperty("paid_at") Instant paidAt, @JsonProperty("transfer_deadline_at") Instant transferDeadlineAt,
        @JsonProperty("seller_confirmed_at") Instant sellerConfirmedAt, @JsonProperty("buyer_confirmed_at") Instant buyerConfirmedAt,
        @JsonProperty("settlement_released_at") Instant settlementReleasedAt, @JsonProperty("buyer_action") String buyerAction,
        @JsonProperty("status_refresh_required") boolean statusRefreshRequired, @JsonProperty("amount_minor") long amountMinor,
        String currency, Event event, Ticket ticket) {
    public record Event(String name, @JsonProperty("starts_at") Instant startsAt, String venue, String city) {}
    public record Ticket(@JsonProperty("ticket_type") String ticketType, @JsonProperty("seat_info") String seatInfo, @JsonProperty("transfer_method") String transferMethod) {}
}
