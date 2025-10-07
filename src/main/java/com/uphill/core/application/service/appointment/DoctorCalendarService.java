package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.AppointmentEventPayload;

public interface DoctorCalendarService {
    boolean updateDoctorCalendar(AppointmentEventPayload event);
}


