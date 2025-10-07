package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.AppointmentEventPayload;

public interface EmailNotificationService {
    boolean sendEmail(AppointmentEventPayload event);
}


