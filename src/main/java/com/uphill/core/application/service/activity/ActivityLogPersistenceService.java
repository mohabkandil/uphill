package com.uphill.core.application.service.activity;

import com.uphill.core.domain.ActivityLog;

public interface ActivityLogPersistenceService {
    ActivityLog save(ActivityLog activityLog);
}


