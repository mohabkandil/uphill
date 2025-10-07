package com.uphill.core.application.service.auth;

import com.uphill.core.auth.TokenProvider;
import com.uphill.core.domain.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthPersistenceService authPersistenceService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    
    public Optional<Admin> authenticate(String email, String password) {
        return authPersistenceService.findByEmail(email)
                .filter(admin -> passwordEncoder.matches(password, admin.getPassword()));
    }
    
    public String generateToken(Admin admin) {
        return tokenProvider.generateToken(admin);
    }
}
