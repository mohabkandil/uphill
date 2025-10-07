package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.OutboxEvent;
import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventPersistenceService {
    List<OutboxEvent> findPendingDueEvents(LocalDateTime now);
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findByAggregateId(Long aggregateId);
}


