package com.uphill.infrastructure.persistence.OutboxEvent;

import com.uphill.core.application.service.appointment.OutboxEventPersistenceService;
import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import com.uphill.core.domain.OutboxEvent;
import com.uphill.core.domain.ActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutboxEventPersistenceServiceImpl implements OutboxEventPersistenceService {

    private final OutboxEventRepository repository;
    private final OutboxEventMapper mapper;
    private final ActivityLogPersistenceService activityLogPersistenceService;

    @Override
    public List<OutboxEvent> findPendingDueEvents(final LocalDateTime now) {
        return repository.findPendingDueEvents(now).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OutboxEvent save(final OutboxEvent event) {
        final OutboxEventEntity entity = mapper.toEntity(event);
        final OutboxEventEntity saved = repository.save(entity);
        
        final ActivityLog activityLog = ActivityLog.builder()
                .userId(0L)
                .action("OUTBOX_EVENT_SAVED")
                .description(String.format("Outbox event %d of type %s saved with status %s for aggregate %d", 
                    saved.getId(), event.getEventType(), event.getStatus(), event.getAggregateId()))
                .createdAt(LocalDateTime.now())
                .build();
        activityLogPersistenceService.save(activityLog);
        
        return mapper.toDomain(saved);
    }

    @Override
    public List<OutboxEvent> findByAggregateId(final Long aggregateId) {
        return repository.findByAggregateId(aggregateId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}


