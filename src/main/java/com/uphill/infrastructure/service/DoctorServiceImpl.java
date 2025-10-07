package com.uphill.infrastructure.service;

import com.uphill.core.application.service.appointment.DoctorService;
import com.uphill.core.exception.AppointmentSlotUnavailableException;
import com.uphill.core.domain.Doctor;
import com.uphill.infrastructure.persistence.Doctor.DoctorEntity;
import com.uphill.infrastructure.persistence.Doctor.DoctorRepository;
import com.uphill.infrastructure.persistence.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final EntityMapper entityMapper;

    @Override
    public Doctor findAvailableDoctor(final Long specialtyId, final LocalDate date, final Long timeSlotId) {
        final List<DoctorEntity> availableDoctors = doctorRepository.findAvailableBySpecialtyAndDateAndTimeSlot(specialtyId, date, timeSlotId);
        if (availableDoctors.isEmpty()) {
            throw new AppointmentSlotUnavailableException("No available doctor for the requested specialty, date, and time slot");
        }
        return entityMapper.toDomain(availableDoctors.get(0));
    }
}


