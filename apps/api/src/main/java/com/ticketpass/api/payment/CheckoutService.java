package com.ticketpass.api.payment;

import com.ticketpass.api.order.OrderStatus;
import com.ticketpass.api.order.SafeOrderResponseService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutService.class);

    private final CheckoutPreparationService preparationService;
    private final PaymentProvider paymentProvider;
    private final SafeOrderResponseService safeOrderResponseService;

    CheckoutService(
            CheckoutPreparationService preparationService,
            PaymentProvider paymentProvider,
            SafeOrderResponseService safeOrderResponseService) {
        this.preparationService = preparationService;
        this.paymentProvider = paymentProvider;
        this.safeOrderResponseService = safeOrderResponseService;
    }

    public CheckoutResult checkout(UUID buyerId, String reservationId) {
        CheckoutPreparation preparation = prepareWithConcurrencyRecovery(buyerId, reservationId);
        LOGGER.info("Checkout order {} {}", preparation.order().getId(),
                preparation.orderCreated() ? "created" : "recovered");
        if (preparation.order().getStatus() == OrderStatus.PAID) {
            return new CheckoutResult(
                    new CheckoutResponse(safeOrderResponseService.forCheckout(preparation.order().getId()), null, null),
                    preparation.orderCreated());
        }

        PaymentSessionResult providerResult;
        try {
            providerResult = preparation.paymentSessionCreated()
                    ? paymentProvider.createSession(toProviderRequest(preparation))
                    : paymentProvider.getSession(preparation.paymentSession().getProviderSessionId());
        } catch (PaymentProviderUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new PaymentProviderUnavailableException();
        }

        PaymentSessionEntity session = preparationService.persistProviderResult(
                preparation.paymentSession().getId(), providerResult);
        String paymentUrl = session.getStatus() == PaymentSessionStatus.PENDING
                ? providerResult.hostedCheckoutUrl()
                : null;
        return new CheckoutResult(
                new CheckoutResponse(
                        safeOrderResponseService.forCheckout(preparation.order().getId()),
                        paymentUrl,
                        paymentUrl == null ? null : session.getExpiresAt()),
                preparation.orderCreated());
    }

    private CheckoutPreparation prepareWithConcurrencyRecovery(UUID buyerId, String reservationId) {
        try {
            return preparationService.prepare(buyerId, reservationId);
        } catch (DataIntegrityViolationException exception) {
            return preparationService.prepare(buyerId, reservationId);
        }
    }

    private static PaymentSessionRequest toProviderRequest(CheckoutPreparation preparation) {
        return new PaymentSessionRequest(
                preparation.order().getId(),
                preparation.paymentSession().getProviderSessionId(),
                preparation.order().getAmountMinor(),
                preparation.order().getCurrency(),
                preparation.order().getExpiresAt());
    }

    public record CheckoutResult(CheckoutResponse response, boolean created) {
    }
}
