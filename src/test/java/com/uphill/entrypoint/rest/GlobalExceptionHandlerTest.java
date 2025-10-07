package com.uphill.entrypoint.rest;

import com.uphill.core.exception.AppointmentSlotUnavailableException;
import com.uphill.entrypoint.rest.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

	@InjectMocks
	private GlobalExceptionHandler handler;

	@Test
	void handleAppointmentSlotUnavailable_ShouldReturnConflictWithProperCode() {
		AppointmentSlotUnavailableException ex = new AppointmentSlotUnavailableException("No room available");
		ResponseEntity<ApiResponse<Void>> response = handler.handleAppointmentSlotUnavailable(ex);
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().isSuccess());
		assertEquals("APPOINTMENT_SLOT_UNAVAILABLE", response.getBody().getErrorCode());
		assertEquals("No room available", response.getBody().getErrorMessage());
	}

	@Test
	void handleDataIntegrityViolation_UniqueConstraint_ShouldReturnConflictWithBookingCode() {
		DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate key value violates unique constraint");
		ResponseEntity<ApiResponse<Void>> response = handler.handleDataIntegrityViolation(ex);
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().isSuccess());
		assertEquals("APPOINTMENT_ALREADY_BOOKED", response.getBody().getErrorCode());
	}
}
