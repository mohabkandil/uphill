package com.uphill.core.application.service.auth;

import com.uphill.core.domain.Admin;
import java.util.Optional;

public interface AuthPersistenceService {

    Optional<Admin> findByEmail(String email);
}
