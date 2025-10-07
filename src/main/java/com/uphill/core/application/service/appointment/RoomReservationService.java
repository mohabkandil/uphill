package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.AppointmentEventPayload;

public interface RoomReservationService {
    boolean reserveRoom(AppointmentEventPayload event);
}


