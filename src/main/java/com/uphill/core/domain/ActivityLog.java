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
public class ActivityLog {
    private Long id;
    private Long userId;
    private String action;
    private String description;
    private LocalDateTime createdAt;
}