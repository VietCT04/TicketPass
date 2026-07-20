package com.ticketpass.api.order;

import com.ticketpass.api.common.ApiException;
import com.ticketpass.api.settlement.SettlementProvider;
import com.ticketpass.api.settlement.SettlementReleaseRequest;
import com.ticketpass.api.settlement.SettlementReleaseResult;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BuyerReceiptConfirmationService {

    private final BuyerReceiptConfirmationCommand confirmationCommand;
    private final OrderRepository orderRepository;
    private final OrderFulfillmentRepository fulfillmentRepository;
    private final SettlementReleaseClaimService claimService;
    private final SettlementProvider settlementProvider;
    private final SettlementReleaseFinalizer finalizer;

    public BuyerReceiptConfirmationService(
            BuyerReceiptConfirmationCommand confirmationCommand,
            OrderRepository orderRepository,
            OrderFulfillmentRepository fulfillmentRepository,
            SettlementReleaseClaimService claimService,
            SettlementProvider settlementProvider,
            SettlementReleaseFinalizer finalizer) {
        this.confirmationCommand = confirmationCommand;
        this.orderRepository = orderRepository;
        this.fulfillmentRepository = fulfillmentRepository;
        this.claimService = claimService;
        this.settlementProvider = settlementProvider;
        this.finalizer = finalizer;
    }

    public BuyerReceiptConfirmationResponse confirm(UUID buyerId, String rawOrderId) {
        UUID orderId = parseOrderId(rawOrderId);
        confirmationCommand.accept(buyerId, orderId);
        executeClaimedRelease(orderId);

        OrderEntity order = orderRepository.findByIdForResponse(orderId)
                .orElseThrow(() -> new IllegalStateException("Confirmed order is missing"));
        OrderFulfillmentEntity fulfillment = fulfillmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Confirmed fulfilment is missing"));
        return response(order, fulfillment);
    }

    private void executeClaimedRelease(UUID orderId) {
        SettlementReleaseOperationEntity operation = claimService.claim(orderId);
        if (operation == null) {
            return;
        }

        OrderEntity order = orderRepository.findByIdForResponse(orderId)
                .orElseThrow(() -> new IllegalStateException("Release order is missing"));
        SettlementReleaseRequest request = new SettlementReleaseRequest(
                orderId,
                order.getAmountMinor(),
                order.getCurrency(),
                operation.getIdempotencyKey(),
                operation.getProviderOperationId());
        SettlementReleaseResult result = settlementProvider.release(request);
        finalizer.finalizeResult(orderId, result);
    }

    private static UUID parseOrderId(String rawOrderId) {
        try {
            return UUID.fromString(rawOrderId);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId must be a UUID");
        }
    }

    private static BuyerReceiptConfirmationResponse response(
            OrderEntity order,
            OrderFulfillmentEntity fulfillment) {
        return new BuyerReceiptConfirmationResponse(
                order.getId().toString(),
                order.getStatus().name(),
                fulfillment.getTransferStatus().name(),
                fulfillment.getSettlementStatus().name(),
                order.getPaidAt(),
                fulfillment.getTransferDeadlineAt(),
                fulfillment.getSellerConfirmedAt(),
                fulfillment.getBuyerConfirmedAt(),
                fulfillment.getSettlementReleasedAt(),
                "NONE",
                fulfillment.getSettlementStatus() != SettlementStatus.RELEASED_TO_SELLER,
                order.getAmountMinor(),
                order.getCurrency(),
                new BuyerReceiptConfirmationResponse.Event(
                        order.getListing().getEvent().getName(),
                        order.getListing().getEvent().getStartsAt(),
                        order.getListing().getEvent().getVenue(),
                        order.getListing().getEvent().getCity()),
                new BuyerReceiptConfirmationResponse.Ticket(
                        order.getListing().getTicketType(),
                        order.getListing().getSeatInfo(),
                        order.getListing().getTransferMethod().name()));
    }
}
