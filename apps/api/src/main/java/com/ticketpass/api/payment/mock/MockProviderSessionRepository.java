package com.ticketpass.api.payment.mock;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MockProviderSessionRepository extends JpaRepository<MockProviderSessionEntity, UUID> {

    Optional<MockProviderSessionEntity> findByProviderSessionId(String providerSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from MockProviderSessionEntity session where session.providerSessionId = :providerSessionId")
    Optional<MockProviderSessionEntity> findByProviderSessionIdForUpdate(
            @Param("providerSessionId") String providerSessionId);
}
