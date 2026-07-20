package com.ticketpass.api.audit;

import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final Clock clock;

    public AuditService(AuditEventRepository auditEventRepository, Clock clock) {
        this.auditEventRepository = auditEventRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordListingCreated(UUID actorUserId, UUID listingId) {
        AuditEventEntity event = new AuditEventEntity();
        event.setActorUserId(actorUserId);
        event.setAction(AuditAction.LISTING_CREATED);
        event.setEntityType(AuditEntityType.LISTING);
        event.setEntityId(listingId);
        event.setCreatedAt(clock.instant());

        auditEventRepository.save(event);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordOrderAction(UUID actorUserId, UUID orderId, AuditAction action, java.time.Instant occurredAt) {
        AuditEventEntity event = new AuditEventEntity();
        event.setActorUserId(actorUserId);
        event.setAction(action);
        event.setEntityType(AuditEntityType.ORDER);
        event.setEntityId(orderId);
        event.setCreatedAt(occurredAt);
        auditEventRepository.save(event);
    }
}
