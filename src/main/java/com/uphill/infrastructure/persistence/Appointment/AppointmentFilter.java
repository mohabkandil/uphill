package com.uphill.infrastructure.persistence.Appointment;

import com.uphill.core.domain.AppointmentStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class AppointmentFilter {
    
    Long patientId;
    Long doctorId;
    Long roomId;
    AppointmentStatus status;
    LocalDate startDate;
    LocalDate endDate;
    
    public static AppointmentFilter byPatient(Long patientId) {
        return AppointmentFilter.builder()
                .patientId(patientId)
                .build();
    }
    
    public static AppointmentFilter byDoctor(Long doctorId) {
        return AppointmentFilter.builder()
                .doctorId(doctorId)
                .build();
    }
    
    public static AppointmentFilter byRoom(Long roomId) {
        return AppointmentFilter.builder()
                .roomId(roomId)
                .build();
    }
    
    public static AppointmentFilter byStatus(AppointmentStatus status) {
        return AppointmentFilter.builder()
                .status(status)
                .build();
    }
    
    public static AppointmentFilter byDateRange(LocalDate startDate, LocalDate endDate) {
        return AppointmentFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
    
    public static AppointmentFilter all() {
        return AppointmentFilter.builder().build();
    }
}
