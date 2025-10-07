package com.uphill.infrastructure.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEventDto {
    @JsonProperty("appointmentId")
    private Long appointmentId;
    
    @JsonProperty("patientId")
    private Long patientId;
    
    @JsonProperty("doctorId")
    private Long doctorId;
    
    @JsonProperty("roomId")
    private Long roomId;
    
    @JsonProperty("timeSlotId")
    private Long timeSlotId;
    
    @JsonProperty("date")
    private LocalDate date;
    
    @JsonProperty("status")
    private String status;
}
