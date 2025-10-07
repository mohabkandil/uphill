package com.uphill.infrastructure.persistence.Appointment;

import com.uphill.core.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    
    @Query(value = "SELECT a.* FROM appointments a " +
           "JOIN doctors d ON d.id = a.doctor_id " +
           "JOIN patients p ON p.id = a.patient_id " +
           "JOIN rooms r ON r.id = a.room_id " +
           "JOIN time_slots t ON t.id = a.time_slot_id " +
           "WHERE (:patientId IS NULL OR a.patient_id = :patientId) " +
           "AND (:doctorId IS NULL OR a.doctor_id = :doctorId) " +
           "AND (:roomId IS NULL OR a.room_id = :roomId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:startDate IS NULL OR a.date >= CAST(:startDate AS date)) " +
           "AND (:endDate IS NULL OR a.date <= CAST(:endDate AS date)) " +
           "ORDER BY a.date DESC, t.start_time ASC, a.id ASC", nativeQuery = true)
    Page<AppointmentEntity> findAppointmentsWithFilters(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("roomId") Long roomId,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable);

    @Modifying
    @Query("UPDATE AppointmentEntity a SET a.status = :status WHERE a.id = :appointmentId")
    int updateAppointmentStatus(@Param("appointmentId") Long appointmentId, @Param("status") AppointmentStatus status);
}
