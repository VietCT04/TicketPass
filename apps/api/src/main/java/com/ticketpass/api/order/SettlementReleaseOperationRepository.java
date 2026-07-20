package com.ticketpass.api.order;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SettlementReleaseOperationRepository extends JpaRepository<SettlementReleaseOperationEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select operation from SettlementReleaseOperationEntity operation where operation.orderId = :orderId")
    Optional<SettlementReleaseOperationEntity> findByOrderIdForUpdate(@Param("orderId") UUID orderId);

    @Query(value = """
            select order_id from settlement_release_operations
            where (status = 'PENDING' or (status = 'RETRYABLE_FAILURE' and next_attempt_at <= :now)
                or (status = 'PROCESSING' and processing_lease_until <= :now))
            order by created_at asc, order_id asc
            """, nativeQuery = true)
    List<UUID> findClaimableOrderIds(@Param("now") Instant now, Pageable pageable);
}
