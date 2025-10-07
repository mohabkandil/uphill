package com.uphill.core.application.service.appointment;

import com.uphill.core.domain.Appointment;
import com.uphill.core.domain.AppointmentStatus;
import com.uphill.core.domain.Doctor;
import com.uphill.core.domain.Room;
import com.uphill.core.domain.TimeSlot;
import com.uphill.core.application.service.activity.ActivityLogHelper;
import com.uphill.core.domain.ActivityEventType;
import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentPersistenceService appointmentPersistenceService;
    private final DoctorService doctorService;
    private final RoomService roomService;
    private final OutboxEventService outboxEventService;
    private final TimeSlotService timeSlotService;
    private final ActivityLogPersistenceService activityLogPersistenceService;

    public Page<Appointment> findAppointmentsWithFilters(
            final Long patientId,
            final Long doctorId,
            final Long roomId,
            final String status,
            final LocalDate startDate,
            final LocalDate endDate,
            final Pageable pageable) {
        
        final AppointmentStatus statusEnum = AppointmentStatus.fromString(status);
        
        return appointmentPersistenceService.findAppointmentsWithFilters(
            patientId, doctorId, roomId, statusEnum, startDate, endDate, pageable);
    }

    @Transactional
    public Appointment createAppointment(final Appointment appointment, final String timeSlotString) {
        final String[] times = timeSlotString.split("-");
        final LocalTime startTime = java.time.LocalTime.parse(times[0]);
        final LocalTime endTime = java.time.LocalTime.parse(times[1]);
        
        final TimeSlot timeSlot = timeSlotService.findByTimeRange(startTime, endTime);
        appointment.setTimeSlot(timeSlot);
        
        final Doctor doctor = doctorService.findAvailableDoctor(
                appointment.getDoctor().getSpecialty().getId(), 
                appointment.getDate(), 
                timeSlot.getId()
        );
        final Room room = roomService.findAvailableRoom(appointment.getDate(), timeSlot.getId());

        appointment.setDoctor(doctor);
        appointment.setRoom(room);

        ActivityLogHelper.logAppointmentEvent(
                activityLogPersistenceService,
                0L,
                ActivityEventType.DOCTOR_SELECTED,
                appointment,
                timeSlotString
        );

        ActivityLogHelper.logAppointmentEvent(
                activityLogPersistenceService,
                0L,
                ActivityEventType.ROOM_SELECTED,
                appointment,
                timeSlotString
        );
        appointment.setStatus(AppointmentStatus.BOOKED);
        
        final Appointment savedAppointment = appointmentPersistenceService.saveAppointment(appointment);

        outboxEventService.createDoctorCalendarUpdateEvent(savedAppointment);
        outboxEventService.createRoomReservationEvent(savedAppointment);
        outboxEventService.createEmailConfirmationEvent(savedAppointment);

        return savedAppointment;
    }
}
