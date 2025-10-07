package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.Room;

import java.time.LocalDate;

public interface RoomService {
    Room findAvailableRoom(LocalDate date, Long timeSlotId);
}


