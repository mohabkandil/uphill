package com.uphill.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    private Long id;
    private String name;
    private String email;
    private String externalId;
    private Specialty specialty;
}