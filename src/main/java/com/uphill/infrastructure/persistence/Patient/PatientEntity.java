package com.uphill.infrastructure.persistence.Patient;

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

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class PatientEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;
    
    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;
    
    @Column(name = "email", nullable = false, unique = true)
    @ToString.Include
    private String email;
    
    @Column(name = "dob", nullable = false)
    private String dob;
    
    @Column(name = "medical_history")
    private String medicalHistory;
}
