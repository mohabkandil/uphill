package com.uphill.core.application.service.appointment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import com.uphill.core.domain.AppointmentEventPayload;
import com.uphill.core.domain.AppointmentStatus;
import com.uphill.core.domain.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxProcessingServiceChallengingTest {

	@Mock
	private OutboxEventPersistenceService outboxEventPersistenceService;
	@Mock
	private AppointmentPersistenceService appointmentPersistenceService;
	@Mock
	private DoctorCalendarService doctorCalendarService;
	@Mock
	private RoomReservationService roomReservationService;
	@Mock
	private EmailNotificationService emailNotificationService;
	@Mock
	private ActivityLogPersistenceService activityLogPersistenceService;
	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private OutboxProcessingService outboxProcessingService;

	@Test
	void pollAndProcess_InvalidPayload_ShouldRetryAndLog() throws Exception {
		OutboxEvent badEvent = OutboxEvent.builder()
				.id(1L)
				.aggregateId(100L)
				.eventType("DOCTOR_CALENDAR_UPDATE")
				.status("PENDING")
				.retryCount(0)
				.payload("{invalid}")
				.nextRetryAt(LocalDateTime.now().minusMinutes(1))
				.build();

		when(outboxEventPersistenceService.findPendingDueEvents(any(LocalDateTime.class)))
				.thenReturn(List.of(badEvent));
        when(objectMapper.readValue(any(String.class), eq(AppointmentEventPayload.class)))
				.thenThrow(new JsonProcessingException("bad json"){});

		outboxProcessingService.pollAndProcess();

		verify(outboxEventPersistenceService, times(1)).save(argThat(e -> e.getRetryCount() == 1));
		verify(activityLogPersistenceService, times(1)).save(argThat(log ->
				"OUTBOX_EVENT_RETRY".equals(log.getAction()) && log.getUserId() == 0L));
		verify(appointmentPersistenceService, never()).updateAppointmentStatus(any(), any());
	}

	@Test
	void pollAndProcess_MaxRetries_ShouldFailAndCancelAppointment() throws Exception {
		OutboxEvent event = OutboxEvent.builder()
				.id(2L)
				.aggregateId(200L)
				.eventType("ROOM_RESERVATION")
				.status("PENDING")
				.retryCount(4)
				.payload("{}")
				.nextRetryAt(LocalDateTime.now().minusMinutes(1))
				.build();

		when(outboxEventPersistenceService.findPendingDueEvents(any(LocalDateTime.class)))
				.thenReturn(List.of(event));
        when(objectMapper.readValue(any(String.class), eq(AppointmentEventPayload.class)))
                .thenReturn(null);
		when(roomReservationService.reserveRoom(any())).thenReturn(false);

		outboxProcessingService.pollAndProcess();

		verify(outboxEventPersistenceService, atLeastOnce()).save(argThat(e -> "FAILED".equals(e.getStatus())));
		verify(activityLogPersistenceService, times(1)).save(argThat(log ->
				"OUTBOX_EVENT_FAILED".equals(log.getAction())));
		verify(appointmentPersistenceService, times(1)).updateAppointmentStatus(200L, AppointmentStatus.CANCELLED);
	}

	@Test
	void pollAndProcess_AllProcessed_ShouldConfirmAppointment() throws Exception {
		OutboxEvent event = OutboxEvent.builder()
				.id(3L)
				.aggregateId(300L)
				.eventType("DOCTOR_CALENDAR_UPDATE")
				.status("PENDING")
				.retryCount(0)
				.payload("{}")
				.nextRetryAt(LocalDateTime.now().minusMinutes(1))
				.build();

		when(outboxEventPersistenceService.findPendingDueEvents(any(LocalDateTime.class)))
				.thenReturn(List.of(event));
        when(objectMapper.readValue(any(String.class), eq(AppointmentEventPayload.class)))
                .thenReturn(null);
		when(doctorCalendarService.updateDoctorCalendar(any())).thenReturn(true);
		when(outboxEventPersistenceService.findByAggregateId(300L))
				.thenReturn(List.of(
					OutboxEvent.builder().status("PROCESSED").build()
				));

		outboxProcessingService.pollAndProcess();

		verify(appointmentPersistenceService, times(1)).updateAppointmentStatus(300L, AppointmentStatus.CONFIRMED);
	}

	@Test
	void pollAndProcess_MixedStatuses_ShouldNotConfirm() throws Exception {
		OutboxEvent event = OutboxEvent.builder()
				.id(4L)
				.aggregateId(400L)
				.eventType("SEND_EMAIL")
				.status("PENDING")
				.retryCount(0)
				.payload("{}")
				.nextRetryAt(LocalDateTime.now().minusMinutes(1))
				.build();

		when(outboxEventPersistenceService.findPendingDueEvents(any(LocalDateTime.class)))
				.thenReturn(List.of(event));
        when(objectMapper.readValue(any(String.class), eq(AppointmentEventPayload.class)))
                .thenReturn(null);
		when(emailNotificationService.sendEmail(any())).thenReturn(true);
		when(outboxEventPersistenceService.findByAggregateId(400L))
				.thenReturn(List.of(
					OutboxEvent.builder().status("PROCESSED").build(),
					OutboxEvent.builder().status("PENDING").build()
				));

		outboxProcessingService.pollAndProcess();

		verify(appointmentPersistenceService, never()).updateAppointmentStatus(any(), any());
	}
}
