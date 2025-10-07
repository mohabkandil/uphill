package com.uphill.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uphill.core.application.service.appointment.OutboxEventService;
import com.uphill.core.domain.Appointment;
import com.uphill.infrastructure.persistence.OutboxEvent.OutboxEventEntity;
import com.uphill.infrastructure.persistence.OutboxEvent.OutboxEventRepository;
import com.uphill.infrastructure.service.dto.AppointmentEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void createDoctorCalendarUpdateEvent(final Appointment appointment) {
        saveEvent(appointment, "DOCTOR_CALENDAR_UPDATE");
    }

    @Override
    public void createRoomReservationEvent(final Appointment appointment) {
        saveEvent(appointment, "ROOM_RESERVATION");
    }

    @Override
    public void createEmailConfirmationEvent(final Appointment appointment) {
        saveEvent(appointment, "SEND_CONFIRMATION_EMAIL");
    }

    private void saveEvent(final Appointment appointment, final String eventType) {
        // Create a simple DTO to avoid serialization issues with complex domain objects
        final AppointmentEventDto eventDto = AppointmentEventDto.builder()
                .appointmentId(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .doctorId(appointment.getDoctor().getId())
                .roomId(appointment.getRoom().getId())
                .timeSlotId(appointment.getTimeSlot().getId())
                .date(appointment.getDate())
                .status(appointment.getStatus().name())
                .build();
        
        final JsonNode payload = objectMapper.valueToTree(eventDto);
        final OutboxEventEntity entity = OutboxEventEntity.builder()
                .aggregateId(appointment.getId())
                .aggregateType("APPOINTMENT")
                .eventType(eventType)
                .payload(payload)
                .status("PENDING")
                .retryCount(0)
                .nextRetryAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        outboxEventRepository.save(entity);
    }
}


