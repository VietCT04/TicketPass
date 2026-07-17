package com.ticketpass.api.eventrequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class EventRequestPersistenceService {
    private final EventRequestRepository eventRequestRepository;
    EventRequestPersistenceService(EventRequestRepository eventRequestRepository) { this.eventRequestRepository = eventRequestRepository; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    EventRequestEntity saveAndFlush(EventRequestEntity request) { return eventRequestRepository.saveAndFlush(request); }
}
