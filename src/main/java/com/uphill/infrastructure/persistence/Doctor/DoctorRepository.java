package com.uphill.infrastructure.persistence.Doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {

    @Query("SELECT d FROM DoctorEntity d WHERE d.specialty.id = :specialtyId AND NOT EXISTS (" +
            "SELECT a.id FROM AppointmentEntity a " +
            "WHERE a.doctor.id = d.id AND a.date = :date AND a.timeSlot.id = :timeSlotId) " +
            "ORDER BY d.id ASC")
    List<DoctorEntity> findAvailableBySpecialtyAndDateAndTimeSlot(@Param("specialtyId") Long specialtyId,
                                                                  @Param("date") LocalDate date,
                                                                  @Param("timeSlotId") Long timeSlotId);
}


