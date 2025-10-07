package com.uphill.infrastructure.persistence.OutboxEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"aggregate_id", "event_type", "status"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class OutboxEventEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;
    
    @Column(name = "aggregate_id", nullable = false)
    @ToString.Include
    private Long aggregateId;
    
    @Column(name = "aggregate_type", nullable = false)
    @ToString.Include
    private String aggregateType;
    
    @Column(name = "event_type", nullable = false)
    @ToString.Include
    private String eventType;
    
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;
    
    @Column(name = "status", nullable = false)
    @ToString.Include
    private String status;
    
    @Column(name = "retry_count", nullable = false)
    @ToString.Include
    private Integer retryCount;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt;
}
