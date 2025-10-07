package com.uphill.infrastructure.persistence.Appointment;

import com.uphill.core.domain.AppointmentStatus;
import com.uphill.infrastructure.persistence.Doctor.DoctorEntity;
import com.uphill.infrastructure.persistence.Patient.PatientEntity;
import com.uphill.infrastructure.persistence.Room.RoomEntity;
import com.uphill.infrastructure.persistence.TimeSlot.TimeSlotEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;

@Entity
@Table(name = "appointments",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"doctor_id", "date", "time_slot_id"}),
           @UniqueConstraint(columnNames = {"room_id", "date", "time_slot_id"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class AppointmentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlotEntity timeSlot;
    
    @Column(name = "date", nullable = false)
    @ToString.Include
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ToString.Include
    private AppointmentStatus status;
}
