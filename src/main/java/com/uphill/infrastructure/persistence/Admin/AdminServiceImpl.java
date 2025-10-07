package com.uphill.infrastructure.persistence.Admin;

import com.uphill.core.application.service.admin.AdminService;
import com.uphill.core.domain.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    
    @Override
    public Optional<Admin> findById(final Long id) {
        return adminRepository.findById(id)
                .map(adminMapper::toDomain);
    }
}
