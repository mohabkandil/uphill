package com.uphill.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    private Long id;
    private Long aggregateId;
    private String aggregateType;
    private String eventType;
    private String payload;
    private String status;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime nextRetryAt;
}