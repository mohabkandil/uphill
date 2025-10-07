package com.uphill.entrypoint.rest.appointments.dto;

import com.uphill.core.domain.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    
    private Long id;
    private String doctorName;
    private String patientName;
    private String roomName;
    private String timeSlot;
    private LocalDate date;
    private AppointmentStatus status;
}
