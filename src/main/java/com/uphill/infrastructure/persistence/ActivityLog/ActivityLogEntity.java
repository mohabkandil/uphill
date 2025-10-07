package com.uphill.infrastructure.persistence.ActivityLog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class ActivityLogEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    @ToString.Include
    private Long userId;
    
    @Column(name = "action", nullable = false)
    @ToString.Include
    private String action;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt;
}
