package com.uphill.infrastructure.persistence.ActivityLog;

import com.uphill.core.domain.ActivityLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityLogPersistenceServiceImplTest {

    @Mock
    private ActivityLogRepository repository;
    @Mock
    private ActivityLogMapper mapper;

    @InjectMocks
    private ActivityLogPersistenceServiceImpl activityLogPersistenceService;

    @Test
    void save_ShouldPersistActivityLog() {
        ActivityLog activityLog = createTestActivityLog();
        ActivityLogEntity entity = createTestEntity();
        ActivityLogEntity savedEntity = createTestEntity();
        savedEntity.setId(1L);

        when(mapper.toEntity(activityLog)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(activityLog);

        ActivityLog result = activityLogPersistenceService.save(activityLog);

        assertNotNull(result);
        verify(repository, times(1)).save(entity);
        verify(mapper, times(1)).toEntity(activityLog);
        verify(mapper, times(1)).toDomain(savedEntity);
    }

    private ActivityLog createTestActivityLog() {
        return ActivityLog.builder()
                .userId(0L)
                .action("TEST_ACTION")
                .description("Test activity log")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ActivityLogEntity createTestEntity() {
        return ActivityLogEntity.builder()
                .userId(0L)
                .action("TEST_ACTION")
                .description("Test activity log")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
