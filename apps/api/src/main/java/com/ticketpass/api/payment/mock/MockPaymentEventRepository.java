package com.ticketpass.api.payment.mock;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MockPaymentEventRepository extends JpaRepository<MockPaymentEventEntity, UUID> {
}
