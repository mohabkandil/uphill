package com.uphill.infrastructure.persistence.Room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    @Query("SELECT r FROM RoomEntity r WHERE NOT EXISTS (" +
            "SELECT a.id FROM AppointmentEntity a " +
            "WHERE a.room.id = r.id AND a.date = :date AND a.timeSlot.id = :timeSlotId) " +
            "ORDER BY r.id ASC")
    List<RoomEntity> findAvailableByDateAndTimeSlot(@Param("date") LocalDate date,
                                                     @Param("timeSlotId") Long timeSlotId);
}


