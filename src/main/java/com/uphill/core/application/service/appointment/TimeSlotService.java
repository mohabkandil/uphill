package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.TimeSlot;

import java.time.LocalTime;

public interface TimeSlotService {
    TimeSlot findById(Long id);
    TimeSlot findByTimeRange(LocalTime startTime, LocalTime endTime);
}
