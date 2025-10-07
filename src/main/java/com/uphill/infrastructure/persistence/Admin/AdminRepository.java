package com.uphill.infrastructure.persistence.Admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {
    Optional<AdminEntity> findByName(String name);
    Optional<AdminEntity> findByEmail(String email);
}
