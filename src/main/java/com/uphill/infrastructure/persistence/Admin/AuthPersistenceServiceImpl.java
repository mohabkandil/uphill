package com.uphill.infrastructure.persistence.Admin;

import com.uphill.core.application.service.auth.AuthPersistenceService;
import com.uphill.core.domain.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthPersistenceServiceImpl implements AuthPersistenceService {
    
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    
    @Override
    public Optional<Admin> findByEmail(final String email) {
        return adminRepository.findByEmail(email)
                .map(adminMapper::toDomain);
    }
}
