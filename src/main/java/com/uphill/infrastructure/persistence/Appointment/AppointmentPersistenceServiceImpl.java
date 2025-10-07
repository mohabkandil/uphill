package com.uphill.infrastructure.persistence.Appointment;

import com.uphill.core.application.service.appointment.AppointmentPersistenceService;
import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import com.uphill.core.domain.Appointment;
import com.uphill.core.domain.AppointmentStatus;
import com.uphill.core.application.service.activity.ActivityLogHelper;
import com.uphill.core.domain.ActivityEventType;
import com.uphill.infrastructure.persistence.Doctor.DoctorEntity;
import com.uphill.infrastructure.persistence.Doctor.DoctorRepository;
import com.uphill.infrastructure.persistence.Patient.PatientEntity;
import com.uphill.infrastructure.persistence.Patient.PatientRepository;
import com.uphill.infrastructure.persistence.Room.RoomEntity;
import com.uphill.infrastructure.persistence.Room.RoomRepository;
import com.uphill.infrastructure.persistence.TimeSlot.TimeSlotEntity;
import com.uphill.infrastructure.persistence.TimeSlot.TimeSlotRepository;
import com.uphill.infrastructure.persistence.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AppointmentPersistenceServiceImpl implements AppointmentPersistenceService {
    
    private final AppointmentRepository appointmentRepository;
    private final EntityMapper entityMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ActivityLogPersistenceService activityLogPersistenceService;
    
    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> findAppointmentsWithFilters(
            final Long patientId,
            final Long doctorId,
            final Long roomId,
            final AppointmentStatus status,
            final LocalDate startDate,
            final LocalDate endDate,
            final Pageable pageable) {
        return appointmentRepository.findAppointmentsWithFilters(
                patientId, doctorId, roomId, 
                status != null ? status.name() : null,
                startDate != null ? startDate.toString() : null,
                endDate != null ? endDate.toString() : null,
                pageable)
                .map(entityMapper::toDomain);
    }

    @Override
    @Transactional
    public Appointment saveAppointment(final Appointment appointment) {
        final PatientEntity patient = patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        final DoctorEntity doctor = doctorRepository.findById(appointment.getDoctor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        final RoomEntity room = roomRepository.findById(appointment.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        final TimeSlotEntity timeSlot = timeSlotRepository.findById(appointment.getTimeSlot().getId())
                .orElseThrow(() -> new IllegalArgumentException("TimeSlot not found"));

        final AppointmentEntity toSave = AppointmentEntity.builder()
                .patient(patient)
                .doctor(doctor)
                .room(room)
                .timeSlot(timeSlot)
                .date(appointment.getDate())
                .status(appointment.getStatus())
                .build();
        final AppointmentEntity saved = appointmentRepository.save(toSave);
        
        ActivityLogHelper.logAppointmentEvent(
                activityLogPersistenceService,
                0L,
                ActivityEventType.APPOINTMENT_CREATED,
                entityMapper.toDomain(saved),
                null
        );
        
        return entityMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        appointmentRepository.updateAppointmentStatus(appointmentId, status);
        
        final Appointment updated = Appointment.builder()
                .id(appointmentId)
                .status(status)
                .build();
        ActivityLogHelper.logAppointmentEvent(
                activityLogPersistenceService,
                0L,
                ActivityEventType.APPOINTMENT_STATUS_UPDATED,
                updated,
                null
        );
    }
}
