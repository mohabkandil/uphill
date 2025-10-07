package com.uphill.infrastructure.persistence.TimeSlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlotEntity, Long> {
    Optional<TimeSlotEntity> findByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);
}


