package com.uphill.entrypoint.rest.appointments.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {
    @NotNull
    @Min(1)
    private Long patientId;
    @NotNull
    @Min(1)
    private Long specialtyId;
    @NotNull
    private LocalDate date;
    @NotNull
    @Pattern(regexp = "^\\d{2}:\\d{2}-\\d{2}:\\d{2}$", message = "Time slot must be in format HH:MM-HH:MM")
    private String timeSlot;
}


