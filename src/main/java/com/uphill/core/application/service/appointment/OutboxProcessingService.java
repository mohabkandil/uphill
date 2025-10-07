package com.uphill.core.application.service.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.uphill.core.domain.AppointmentEventPayload;
import com.uphill.core.domain.AppointmentStatus;
import com.uphill.core.domain.OutboxEvent;
import com.uphill.core.domain.ActivityLog;
import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxProcessingService {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessingService.class);

    private final OutboxEventPersistenceService outboxEventPersistenceService;
    private final AppointmentPersistenceService appointmentPersistenceService;
    private final DoctorCalendarService doctorCalendarService;
    private final RoomReservationService roomReservationService;
    private final EmailNotificationService emailNotificationService;
    private final ObjectMapper objectMapper;
    private final ActivityLogPersistenceService activityLogPersistenceService;

    @Scheduled(fixedDelayString = "${outbox.poll.interval.ms:10000}")
    @Transactional
    public void pollAndProcess() {
        final List<OutboxEvent> dueEvents = outboxEventPersistenceService.findPendingDueEvents(LocalDateTime.now());
        log.info("Found {} pending events to process", dueEvents.size());
        for (final OutboxEvent event : dueEvents) {
            try {
                log.info("Processing event {} of type {}", event.getId(), event.getEventType());
                processSingleEvent(event);
            } catch (Exception ex) {
                log.error("Unexpected error processing event {}: {}", event.getId(), ex.getMessage(), ex);
                handleFailure(event);
            }
        }
    }

    private void processSingleEvent(final OutboxEvent event) {
        final AppointmentEventPayload payload;
        try {
            payload = objectMapper.readValue(event.getPayload(), AppointmentEventPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid payload for event {}: {}", event.getId(), e.getMessage());
            handleFailure(event);
            return;
        }
        final boolean success = switch (event.getEventType()) {
            case "DOCTOR_CALENDAR_UPDATE" -> doctorCalendarService.updateDoctorCalendar(payload);
            case "ROOM_RESERVATION" -> roomReservationService.reserveRoom(payload);
            case "SEND_EMAIL", "SEND_CONFIRMATION_EMAIL" -> emailNotificationService.sendEmail(payload);
            default -> {
                log.warn("Unknown event type: {}", event.getEventType());
                yield false;
            }
        };

        if (success) {
            event.setStatus("PROCESSED");
            outboxEventPersistenceService.save(event);
            
            final ActivityLog activityLog = ActivityLog.builder()
                    .userId(0L)
                    .action("OUTBOX_EVENT_PROCESSED")
                    .description(String.format("Outbox event %d of type %s successfully processed for aggregate %d", 
                        event.getId(), event.getEventType(), event.getAggregateId()))
                    .createdAt(LocalDateTime.now())
                    .build();
            activityLogPersistenceService.save(activityLog);
            
            checkAndConfirmAppointment(event.getAggregateId());
        } else {
            handleFailure(event);
        }
    }

    private void checkAndConfirmAppointment(final Long appointmentId) {
        final List<OutboxEvent> appointmentEvents = outboxEventPersistenceService.findByAggregateId(appointmentId);
        
        final boolean allProcessed = appointmentEvents.stream()
            .allMatch(event -> "PROCESSED".equals(event.getStatus()));
            
        if (allProcessed && !appointmentEvents.isEmpty()) {
            log.info("All outbox events processed for appointment {}, confirming appointment", appointmentId);
            appointmentPersistenceService.updateAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);
            
            // TODO: Notify that the appointment is confirmed either via emails, Kafka messages, etc.
        }
    }

    private void handleFailure(final OutboxEvent event) {
        final int newRetry = event.getRetryCount() + 1;
        event.setRetryCount(newRetry);
        if (newRetry >= 5) {
            event.setStatus("FAILED");
            outboxEventPersistenceService.save(event);
            
            final ActivityLog activityLog = ActivityLog.builder()
                    .userId(0L)
                    .action("OUTBOX_EVENT_FAILED")
                    .description(String.format("Outbox event %d of type %s failed after %d retries for aggregate %d", 
                        event.getId(), event.getEventType(), newRetry, event.getAggregateId()))
                    .createdAt(LocalDateTime.now())
                    .build();
            activityLogPersistenceService.save(activityLog);
            
            appointmentPersistenceService.updateAppointmentStatus(event.getAggregateId(), AppointmentStatus.CANCELLED);
        } else {
            final long delayMinutes = (long) Math.pow(2, newRetry - 1);
            event.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            outboxEventPersistenceService.save(event);
            
            final ActivityLog activityLog = ActivityLog.builder()
                    .userId(0L)
                    .action("OUTBOX_EVENT_RETRY")
                    .description(String.format("Outbox event %d of type %s scheduled for retry %d in %d minutes for aggregate %d", 
                        event.getId(), event.getEventType(), newRetry, delayMinutes, event.getAggregateId()))
                    .createdAt(LocalDateTime.now())
                    .build();
            activityLogPersistenceService.save(activityLog);
        }
    }
}


