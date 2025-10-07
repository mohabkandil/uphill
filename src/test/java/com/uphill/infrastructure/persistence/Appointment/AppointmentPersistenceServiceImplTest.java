package com.uphill.infrastructure.persistence.Appointment;

import com.uphill.core.application.service.activity.ActivityLogPersistenceService;
import com.uphill.core.domain.ActivityLog;
import com.uphill.core.domain.Appointment;
import com.uphill.core.domain.AppointmentStatus;
import com.uphill.core.domain.Doctor;
import com.uphill.core.domain.Patient;
import com.uphill.core.domain.Room;
import com.uphill.core.domain.TimeSlot;
import com.uphill.infrastructure.persistence.Doctor.DoctorRepository;
import com.uphill.infrastructure.persistence.Patient.PatientRepository;
import com.uphill.infrastructure.persistence.Room.RoomRepository;
import com.uphill.infrastructure.persistence.TimeSlot.TimeSlotRepository;
import com.uphill.infrastructure.persistence.EntityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentPersistenceServiceImplTest {

	@Mock
	private AppointmentRepository appointmentRepository;
	@Mock
	private EntityMapper entityMapper;
	@Mock
	private PatientRepository patientRepository;
	@Mock
	private DoctorRepository doctorRepository;
	@Mock
	private RoomRepository roomRepository;
	@Mock
	private TimeSlotRepository timeSlotRepository;
	@Mock
	private ActivityLogPersistenceService activityLogPersistenceService;

	@InjectMocks
	private AppointmentPersistenceServiceImpl appointmentPersistenceService;

	@Test
	void saveAppointment_ShouldLogAppointmentCreation() {
		// Given
		Appointment appointment = createTestAppointment();
		when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(createPatientEntity()));
		when(doctorRepository.findById(1L)).thenReturn(java.util.Optional.of(createDoctorEntity()));
		when(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(createRoomEntity()));
		when(timeSlotRepository.findById(1L)).thenReturn(java.util.Optional.of(createTimeSlotEntity()));
		when(appointmentRepository.save(any())).thenReturn(createAppointmentEntity());
		when(entityMapper.toDomain(any(AppointmentEntity.class))).thenReturn(appointment);

		// When
		appointmentPersistenceService.saveAppointment(appointment);

		// Then
		verify(activityLogPersistenceService, times(1)).save(argThat(log -> 
			"APPOINTMENT_CREATED".equals(log.getAction()) && log.getUserId() == 0L));
	}

	@Test
	void saveAppointment_ShouldCaptureDescriptiveMessage() {
		// Given
		Appointment appointment = createTestAppointment();
		when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(createPatientEntity()));
		when(doctorRepository.findById(1L)).thenReturn(java.util.Optional.of(createDoctorEntity()));
		when(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(createRoomEntity()));
		when(timeSlotRepository.findById(1L)).thenReturn(java.util.Optional.of(createTimeSlotEntity()));
		when(appointmentRepository.save(any())).thenReturn(createAppointmentEntity());
		when(entityMapper.toDomain(any(AppointmentEntity.class))).thenReturn(appointment);

		ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);

		// When
		appointmentPersistenceService.saveAppointment(appointment);

		// Then
		verify(activityLogPersistenceService).save(logCaptor.capture());
		ActivityLog log = logCaptor.getValue();
		assertEquals("APPOINTMENT_CREATED", log.getAction());
		assertNotNull(log.getDescription());
	}

	@Test
	void saveAppointment_WhenPatientMissing_ShouldThrow() {
		// Given
		Appointment appointment = createTestAppointment();
		when(patientRepository.findById(1L)).thenReturn(java.util.Optional.empty());

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> appointmentPersistenceService.saveAppointment(appointment));
		verify(activityLogPersistenceService, never()).save(any());
	}

	@Test
	void saveAppointment_WhenDoctorMissing_ShouldThrow() {
		Appointment appointment = createTestAppointment();
		when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(createPatientEntity()));
		when(doctorRepository.findById(1L)).thenReturn(java.util.Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> appointmentPersistenceService.saveAppointment(appointment));
		verify(activityLogPersistenceService, never()).save(any());
	}

	@Test
	void saveAppointment_WhenRoomMissing_ShouldThrow() {
		Appointment appointment = createTestAppointment();
		when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(createPatientEntity()));
		when(doctorRepository.findById(1L)).thenReturn(java.util.Optional.of(createDoctorEntity()));
		when(roomRepository.findById(1L)).thenReturn(java.util.Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> appointmentPersistenceService.saveAppointment(appointment));
		verify(activityLogPersistenceService, never()).save(any());
	}

	@Test
	void saveAppointment_WhenTimeSlotMissing_ShouldThrow() {
		Appointment appointment = createTestAppointment();
		when(patientRepository.findById(1L)).thenReturn(java.util.Optional.of(createPatientEntity()));
		when(doctorRepository.findById(1L)).thenReturn(java.util.Optional.of(createDoctorEntity()));
		when(roomRepository.findById(1L)).thenReturn(java.util.Optional.of(createRoomEntity()));
		when(timeSlotRepository.findById(1L)).thenReturn(java.util.Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> appointmentPersistenceService.saveAppointment(appointment));
		verify(activityLogPersistenceService, never()).save(any());
	}

	@Test
	void updateAppointmentStatus_ShouldLogStatusUpdate() {
		// Given
		Long appointmentId = 1L;
		AppointmentStatus status = AppointmentStatus.CONFIRMED;

		// When
		appointmentPersistenceService.updateAppointmentStatus(appointmentId, status);

		// Then
		verify(appointmentRepository, times(1)).updateAppointmentStatus(appointmentId, status);
		verify(activityLogPersistenceService, times(1)).save(argThat(log -> 
			"APPOINTMENT_STATUS_UPDATED".equals(log.getAction()) && log.getUserId() == 0L));
	}

	private Appointment createTestAppointment() {
		return Appointment.builder()
				.patient(Patient.builder().id(1L).build())
				.doctor(Doctor.builder().id(1L).build())
				.room(Room.builder().id(1L).build())
				.timeSlot(TimeSlot.builder().id(1L).build())
				.date(LocalDate.of(2024, 1, 15))
				.status(AppointmentStatus.BOOKED)
				.build();
	}

	private com.uphill.infrastructure.persistence.Patient.PatientEntity createPatientEntity() {
		return com.uphill.infrastructure.persistence.Patient.PatientEntity.builder()
				.id(1L)
				.build();
	}

	private com.uphill.infrastructure.persistence.Doctor.DoctorEntity createDoctorEntity() {
		return com.uphill.infrastructure.persistence.Doctor.DoctorEntity.builder()
				.id(1L)
				.build();
	}

	private com.uphill.infrastructure.persistence.Room.RoomEntity createRoomEntity() {
		return com.uphill.infrastructure.persistence.Room.RoomEntity.builder()
				.id(1L)
				.build();
	}

	private com.uphill.infrastructure.persistence.TimeSlot.TimeSlotEntity createTimeSlotEntity() {
		return com.uphill.infrastructure.persistence.TimeSlot.TimeSlotEntity.builder()
				.id(1L)
				.build();
	}

	private AppointmentEntity createAppointmentEntity() {
		return AppointmentEntity.builder()
				.id(1L)
				.build();
	}
}
