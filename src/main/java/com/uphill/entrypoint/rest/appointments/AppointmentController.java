package com.uphill.entrypoint.rest.appointments;

import com.uphill.core.application.service.appointment.AppointmentService;
import com.uphill.core.domain.Appointment;
import com.uphill.entrypoint.rest.common.response.ApiResponse;
import com.uphill.entrypoint.rest.appointments.dto.AppointmentResponse;
import com.uphill.entrypoint.rest.appointments.mapper.AppointmentMapper;
import com.uphill.entrypoint.rest.appointments.dto.CreateAppointmentRequest;
import com.uphill.entrypoint.rest.appointments.dto.CreateAppointmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointments(
            @RequestParam(required = false) final Long patientId,
            @RequestParam(required = false) final Long doctorId,
            @RequestParam(required = false) final Long roomId,
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) final Pageable pageable) {
        
        final Page<Appointment> appointments = appointmentService.findAppointmentsWithFilters(
                patientId, doctorId, roomId, status, startDate, endDate, pageable);
        
        final Page<AppointmentResponse> response = appointments.map(appointmentMapper::toResponse);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<CreateAppointmentResponse>> createAppointment(@Valid @RequestBody final CreateAppointmentRequest request) {
        final Appointment appointment = appointmentService.createAppointment(appointmentMapper.toDomain(request), request.getTimeSlot());
        final CreateAppointmentResponse response = appointmentMapper.toCreateResponse(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
