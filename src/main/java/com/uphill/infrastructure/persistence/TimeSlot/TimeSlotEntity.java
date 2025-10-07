package com.uphill.infrastructure.persistence.TimeSlot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"start_time", "end_time"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class TimeSlotEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;
    
    @Column(name = "start_time", nullable = false)
    @ToString.Include
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    @ToString.Include
    private LocalTime endTime;
}
