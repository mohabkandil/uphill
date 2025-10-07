package com.uphill.entrypoint.rest.appointments.mapper;

import com.uphill.core.domain.Appointment;
import com.uphill.entrypoint.rest.appointments.dto.AppointmentResponse;
import com.uphill.entrypoint.rest.appointments.dto.CreateAppointmentRequest;
import com.uphill.entrypoint.rest.appointments.dto.CreateAppointmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = { com.uphill.core.domain.Patient.class, com.uphill.core.domain.Doctor.class, com.uphill.core.domain.Specialty.class })
public interface AppointmentMapper {

    @Mapping(target = "doctorName", expression = "java(appointment.getDoctor() != null ? appointment.getDoctor().getName() : \"N/A\")")
    @Mapping(target = "patientName", expression = "java(appointment.getPatient() != null ? appointment.getPatient().getName() : \"N/A\")")
    @Mapping(target = "roomName", expression = "java(appointment.getRoom() != null ? appointment.getRoom().getName() : \"N/A\")")
    @Mapping(target = "timeSlot", expression = "java(appointment.getTimeSlot() != null ? appointment.getTimeSlot().getStartTime() + \" - \" + appointment.getTimeSlot().getEndTime() : \"N/A\")")
    AppointmentResponse toResponse(Appointment appointment);

    @Mapping(target = "appointmentId", source = "id")
    @Mapping(target = "doctorName", expression = "java(appointment.getDoctor() != null ? appointment.getDoctor().getName() : \"N/A\")")
    @Mapping(target = "roomName", expression = "java(appointment.getRoom() != null ? appointment.getRoom().getName() : \"N/A\")")
    @Mapping(target = "timeSlot", expression = "java(appointment.getTimeSlot() != null ? appointment.getTimeSlot().getStartTime() + \" - \" + appointment.getTimeSlot().getEndTime() : \"N/A\")")
    CreateAppointmentResponse toCreateResponse(Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "date", source = "date")
    @Mapping(target = "patient", expression = "java(Patient.builder().id(request.getPatientId()).build())")
    @Mapping(target = "timeSlot", ignore = true)
    @Mapping(target = "doctor", expression = "java(Doctor.builder().specialty(Specialty.builder().id(request.getSpecialtyId()).build()).build())")
    Appointment toDomain(CreateAppointmentRequest request);
}
