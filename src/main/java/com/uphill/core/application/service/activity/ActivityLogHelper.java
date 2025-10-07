package com.uphill.core.application.service.activity;

import com.uphill.core.domain.ActivityEventType;
import com.uphill.core.domain.ActivityLog;
import com.uphill.core.domain.Appointment;

import java.time.LocalDateTime;

public final class ActivityLogHelper {

    private ActivityLogHelper() {}

    public static void log(final ActivityLogPersistenceService activityLogPersistenceService,
                           final long userId,
                           final String action,
                           final String description) {
        final ActivityLog logEntry = ActivityLog.builder()
                .userId(userId)
                .action(action)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        activityLogPersistenceService.save(logEntry);
    }

    public static void logAppointmentEvent(final ActivityLogPersistenceService activityLogPersistenceService,
                                           final long userId,
                                           final ActivityEventType eventType,
                                           final Appointment appointment,
                                           final String timeSlotString) {
        final String description = ActivityDescriptionRegistry.build(eventType, appointment, timeSlotString);
        log(activityLogPersistenceService, userId, eventType.name(), description);
    }
}


