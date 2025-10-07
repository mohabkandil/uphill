package com.uphill.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    private Long id;
    private String name;
    private String email;
    private String dob;
    private String medicalHistory;
}