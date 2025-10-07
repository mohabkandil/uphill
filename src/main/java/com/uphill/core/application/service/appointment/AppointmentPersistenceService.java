package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.Appointment;
import com.uphill.core.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AppointmentPersistenceService {
    
    Page<Appointment> findAppointmentsWithFilters(
            Long patientId,
            Long doctorId,
            Long roomId,
            AppointmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);

    Appointment saveAppointment(Appointment appointment);

    void updateAppointmentStatus(Long appointmentId, AppointmentStatus status);
}
