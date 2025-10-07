package com.uphill.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    private Long id;
    private Doctor doctor;
    private Patient patient;
    private Room room;
    private TimeSlot timeSlot;
    private LocalDate date;
    private AppointmentStatus status;
}