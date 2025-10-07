package com.uphill.infrastructure.service;

import com.uphill.core.application.service.appointment.TimeSlotService;
import com.uphill.core.domain.TimeSlot;
import com.uphill.infrastructure.persistence.EntityMapper;
import com.uphill.infrastructure.persistence.TimeSlot.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final EntityMapper entityMapper;

    @Override
    public TimeSlot findById(final Long id) {
        return timeSlotRepository.findById(id)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("TimeSlot not found"));
    }

    @Override
    public TimeSlot findByTimeRange(final LocalTime startTime, final LocalTime endTime) {
        return timeSlotRepository.findByStartTimeAndEndTime(startTime, endTime)
                .map(entityMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("TimeSlot not found for range: " + startTime + "-" + endTime));
    }
}
