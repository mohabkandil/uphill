package com.uphill.core.application.service.admin;

import com.uphill.core.domain.Admin;
import java.util.Optional;

public interface AdminService {
    
    Optional<Admin> findById(Long id);
}
