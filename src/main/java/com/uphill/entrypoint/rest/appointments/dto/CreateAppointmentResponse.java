package com.uphill.entrypoint.rest.appointments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentResponse {
    private Long appointmentId;
    private String doctorName;
    private String roomName;
    private LocalDate date;
    private String timeSlot;
}


