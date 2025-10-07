package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.Doctor;

import java.time.LocalDate;

public interface DoctorService {
    Doctor findAvailableDoctor(Long specialtyId, LocalDate date, Long timeSlotId);
}


