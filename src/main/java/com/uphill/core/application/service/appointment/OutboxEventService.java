package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.Appointment;

public interface OutboxEventService {
    void createDoctorCalendarUpdateEvent(Appointment appointment);
    void createRoomReservationEvent(Appointment appointment);
    void createEmailConfirmationEvent(Appointment appointment);
}


