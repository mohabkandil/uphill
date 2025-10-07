package com.uphill.infrastructure.persistence;

import com.uphill.core.domain.Admin;
import com.uphill.core.domain.Doctor;
import com.uphill.core.domain.Patient;
import com.uphill.core.domain.Room;
import com.uphill.core.domain.Specialty;
import com.uphill.core.domain.TimeSlot;
import com.uphill.infrastructure.persistence.Admin.AdminEntity;
import com.uphill.infrastructure.persistence.Doctor.DoctorEntity;
import com.uphill.infrastructure.persistence.Patient.PatientEntity;
import com.uphill.infrastructure.persistence.Room.RoomEntity;
import com.uphill.infrastructure.persistence.Specialty.SpecialtyEntity;
import com.uphill.infrastructure.persistence.TimeSlot.TimeSlotEntity;
import com.uphill.infrastructure.persistence.Appointment.AppointmentEntity;
import com.uphill.core.domain.Appointment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    
    Admin toDomain(AdminEntity jpaAdmin);
    
    Doctor toDomain(DoctorEntity jpaDoctor);
    
    Patient toDomain(PatientEntity jpaPatient);
    
    Room toDomain(RoomEntity jpaRoom);
    
    Specialty toDomain(SpecialtyEntity jpaSpecialty);
    
    TimeSlot toDomain(TimeSlotEntity jpaTimeSlot);
    
    Appointment toDomain(AppointmentEntity jpaAppointment);
}
