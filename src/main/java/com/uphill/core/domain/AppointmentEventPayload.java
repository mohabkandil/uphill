package com.uphill.core.domain;

import java.time.LocalDate;

public class AppointmentEventPayload {
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Long roomId;
    private Long timeSlotId;
    private LocalDate date;
    private String status;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}


