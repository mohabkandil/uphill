package com.uphill.infrastructure.persistence.ActivityLog;

import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import com.uphill.core.domain.ActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogPersistenceServiceImpl implements ActivityLogPersistenceService {
    
    private final ActivityLogRepository repository;
    private final ActivityLogMapper mapper;
    
    @Override
    public ActivityLog save(final ActivityLog activityLog) {
        final ActivityLogEntity entity = mapper.toEntity(activityLog);
        final ActivityLogEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
