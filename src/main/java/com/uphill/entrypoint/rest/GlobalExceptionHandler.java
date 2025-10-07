package com.uphill.entrypoint.rest;

import com.uphill.entrypoint.rest.common.response.ApiResponse;
import com.uphill.core.exception.InvalidAppointmentStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import com.uphill.core.exception.AppointmentSlotUnavailableException;
import org.hibernate.query.QueryArgumentException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(final MethodArgumentNotValidException ex) {
        log.warn("[GlobalExceptionHandler] Validation exception: {}", ex.getMessage());
        
        final String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("VALIDATION_ERROR", errorMessage));
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(final jakarta.validation.ConstraintViolationException ex) {
        log.warn("[GlobalExceptionHandler] Constraint violation: {}", ex.getMessage());
        final String errorMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Constraint violation");
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("CONSTRAINT_VIOLATION", errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {
        log.warn("[GlobalExceptionHandler] Argument type mismatch: {}", ex.getMessage());
        final String name = ex.getName();
        final String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required type";
        final String msg = String.format("Parameter '%s' must be of type %s", name, requiredType);
        return ResponseEntity.badRequest().body(ApiResponse.failure("TYPE_MISMATCH", msg));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(final MissingServletRequestParameterException ex) {
        log.warn("[GlobalExceptionHandler] Missing request parameter: {}", ex.getParameterName());
        final String msg = String.format("Missing required parameter '%s'", ex.getParameterName());
        return ResponseEntity.badRequest().body(ApiResponse.failure("MISSING_PARAMETER", msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(final HttpMessageNotReadableException ex) {
        log.warn("[GlobalExceptionHandler] Malformed JSON request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure("MALFORMED_JSON", "Malformed or unreadable request body"));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(final NoHandlerFoundException ex) {
        log.warn("[GlobalExceptionHandler] No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("NOT_FOUND", "The requested resource was not found"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(final HttpRequestMethodNotSupportedException ex) {
        log.warn("[GlobalExceptionHandler] Method not supported: {}", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.failure("METHOD_NOT_ALLOWED", "HTTP method not supported for this endpoint"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex) {
        log.warn("[GlobalExceptionHandler] Media type not supported: {}", ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.failure("UNSUPPORTED_MEDIA_TYPE", "Content-Type not supported"));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotAcceptable(final HttpMediaTypeNotAcceptableException ex) {
        log.warn("[GlobalExceptionHandler] Not acceptable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ApiResponse.failure("NOT_ACCEPTABLE", "Requested media type is not acceptable"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(final IllegalArgumentException ex) {
        log.warn("[GlobalExceptionHandler] IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(InvalidAppointmentStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAppointmentStatus(final InvalidAppointmentStatusException ex) {
        log.warn("[GlobalExceptionHandler] Invalid appointment status: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("INVALID_APPOINTMENT_STATUS", ex.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(final Exception ex) {
        log.warn("[GlobalExceptionHandler] Authentication/Authorization exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure("AUTHENTICATION_REQUIRED", "Authentication required. Please provide a valid JWT token."));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(final ObjectOptimisticLockingFailureException ex) {
        log.warn("[GlobalExceptionHandler] Optimistic locking failure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("OPTIMISTIC_LOCKING_FAILURE", "Concurrent modification detected. Please retry your request."));
    }

    @ExceptionHandler(AppointmentSlotUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppointmentSlotUnavailable(final AppointmentSlotUnavailableException ex) {
        log.warn("[GlobalExceptionHandler] Appointment slot unavailable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("APPOINTMENT_SLOT_UNAVAILABLE", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(final DataIntegrityViolationException ex) {
        log.warn("[GlobalExceptionHandler] Data integrity violation: {}", ex.getMessage());
        
        final String message = ex.getMessage();
        if (message != null && (message.contains("unique") || message.contains("duplicate"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure("APPOINTMENT_ALREADY_BOOKED", 
                            "The requested time slot is no longer available. Please try a different time."));
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("DATA_INTEGRITY_VIOLATION", 
                        "The request conflicts with existing data. Please try again."));
    }

    @ExceptionHandler(QueryArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleQueryArgumentException(final QueryArgumentException ex) {
        log.warn("[GlobalExceptionHandler] Query argument exception: {}", ex.getMessage());
        
        final String message = ex.getMessage();
        if (message != null) {
            if (message.contains("AppointmentStatus")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure("INVALID_STATUS_FILTER", 
                                "Invalid status filter value. Valid values are: BOOKED, CONFIRMED, CANCELLED, COMPLETED"));
            }
            if (message.contains("LocalDate")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure("INVALID_DATE_FILTER", 
                                "Invalid date format for filter. Use YYYY-MM-DD format (e.g., 2025-10-08)"));
            }
            if (message.contains("Long")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure("INVALID_ID_FILTER", 
                                "Invalid ID filter value. Must be a valid number"));
            }
        }
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure("INVALID_FILTER", "Invalid filter parameter value"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(final Exception ex) {
        log.error("[GlobalExceptionHandler] Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later."));
    }
}
