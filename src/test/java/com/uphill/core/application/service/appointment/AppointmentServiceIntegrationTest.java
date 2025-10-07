package com.uphill.core.application.service.appointment;

import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import com.uphill.core.domain.Appointment;
import com.uphill.core.domain.AppointmentStatus;
import com.uphill.core.domain.Doctor;
import com.uphill.core.domain.Patient;
import com.uphill.core.domain.Room;
import com.uphill.core.domain.Specialty;
import com.uphill.core.domain.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceIntegrationTest {

    @Mock
    private AppointmentPersistenceService appointmentPersistenceService;
    @Mock
    private DoctorService doctorService;
    @Mock
    private RoomService roomService;
    @Mock
    private OutboxEventService outboxEventService;
    @Mock
    private TimeSlotService timeSlotService;
    @Mock
    private ActivityLogPersistenceService activityLogPersistenceService;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void createAppointment_ShouldGenerateCompleteActivityLogs() {
        // Given
        Appointment appointment = createTestAppointment();
        String timeSlotString = "09:00-10:00";
        
        Doctor doctor = createTestDoctor();
        Room room = createTestRoom();
        TimeSlot timeSlot = createTestTimeSlot();
        Appointment savedAppointment = createTestAppointment();
        savedAppointment.setId(1L);

        when(timeSlotService.findByTimeRange(any(LocalTime.class), any(LocalTime.class))).thenReturn(timeSlot);
        when(doctorService.findAvailableDoctor(any(), any(), any())).thenReturn(doctor);
        when(roomService.findAvailableRoom(any(), any())).thenReturn(room);
        when(appointmentPersistenceService.saveAppointment(any())).thenReturn(savedAppointment);

        // When
        appointmentService.createAppointment(appointment, timeSlotString);

        // Then - Verify all activity logs are created
        verify(activityLogPersistenceService, times(1)).save(argThat(log -> 
            "DOCTOR_SELECTED".equals(log.getAction()) && log.getUserId() == 0L));
        verify(activityLogPersistenceService, times(1)).save(argThat(log -> 
            "ROOM_SELECTED".equals(log.getAction()) && log.getUserId() == 0L));
        
        // Verify outbox events are created
        verify(outboxEventService, times(1)).createDoctorCalendarUpdateEvent(any());
        verify(outboxEventService, times(1)).createRoomReservationEvent(any());
        verify(outboxEventService, times(1)).createEmailConfirmationEvent(any());
    }

    private Appointment createTestAppointment() {
        return Appointment.builder()
                .patient(Patient.builder().id(1L).build())
                .doctor(Doctor.builder().id(1L).specialty(Specialty.builder().id(1L).build()).build())
                .date(LocalDate.of(2024, 1, 15))
                .status(AppointmentStatus.BOOKED)
                .build();
    }

    private Doctor createTestDoctor() {
        return Doctor.builder()
                .id(1L)
                .name("Dr. Test")
                .specialty(Specialty.builder().id(1L).build())
                .build();
    }

    private Room createTestRoom() {
        return Room.builder()
                .id(1L)
                .name("Room 101")
                .build();
    }

    private TimeSlot createTestTimeSlot() {
        return TimeSlot.builder()
                .id(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
    }
}
